package com.example.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.example.common.core.utils.JwtUtils;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.HttpConstants;
import com.example.common.core.domain.LoginUser;
import com.example.common.core.domain.R;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.enums.UserIdentity;
import com.example.common.redis.service.RedisService;
import com.example.gateway.properties.IgnoreWhiteProperties;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * ⽹关鉴权
 *
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    // 排除过滤的 uri ⽩名单地址，在nacos⾃⾏添加
    @Autowired
    private IgnoreWhiteProperties ignoreWhite;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private RedisService redisService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        //请求的接口地址
        String url = request.getURI().getPath();
        // 跳过不需要验证的路径:接口白名单
        if (matches(url, ignoreWhite.getWhites())) {
            //放行
            return chain.filter(exchange);
        }
        //接口不在白名单中
        //从http请求头中获取token
        String token = getToken(request);
        if (StrUtil.isEmpty(token)) {
            return unauthorizedResponse(exchange, "令牌不能为空");
        }
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret); //获取令牌中信息 解析payload中信息
            if (claims == null) {
                return unauthorizedResponse(exchange, "令牌已过期或验证不正确！");
            }
        } catch (Exception e) {
            return unauthorizedResponse(exchange, "令牌已过期或验证不正确！");
        }

        String userKey = JwtUtils.getUserKey(claims); //获取jwt中的key
        //通过key判断redis中数据是否存在
        //redis中数据是否过期 -> JWT中是否过期
        boolean isLogin = redisService.hasKey(getTokenKey(userKey));
        if (!isLogin) {
            return unauthorizedResponse(exchange, "登录状态已过期");
        }
        String userId = JwtUtils.getUserId(claims); //判断jwt中的信息是否完整
        if (StrUtil.isEmpty(userId)) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }

        //JWT正确且未过期
        //判断redis中存储关于用户身份认证的信息正确性
        //得到身份
        LoginUser user = redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);
        //判断请求是C端还是B端
        //B端
        if (url.contains(HttpConstants.SYSTEM_URL_PREFIX) && !UserIdentity.ADMIN.getValue().equals(user.getIdentity())) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }
        //C端
        if (url.contains(HttpConstants.FRIEND_URL_PREFIX) && !UserIdentity.ORDINARY.getValue().equals(user.getIdentity())) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }


        return chain.filter(exchange);
    }

    /**
     * 查找指定url是否匹配指定匹配规则链表中的任意⼀个字符串
     *
     * @param url 指定url
     * @param patternList 需要检查的匹配规则链表
     * @return 是否匹配
     */
    private boolean matches(String url, List<String> patternList) {
        if (StrUtil.isEmpty(url) || CollectionUtils.isEmpty(patternList)) {
            return false;
        }
        for (String pattern : patternList) {
            if (isMatch(pattern, url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断url是否与规则匹配
     * 匹配规则中：
     * ? 表⽰单个字符;
     * * 表⽰⼀层路径内的任意字符串，不可跨层级;
     * ** 表⽰任意层路径;
     *
     * @param pattern 匹配规则
     * @param url 需要匹配的url
     * @return 是否匹配
     */
    private boolean isMatch(String pattern, String url) {
        //pattern 匹配规则
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    /**
     * 获取缓存key
     */
    private String getTokenKey(String token) {
        return CacheConstants.LOGIN_TOKEN_KEY + token;
    }

    /**
     * 从请求头中获取请求token
     */
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(HttpConstants.AUTHENTICATION);
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return token;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg) {
        log.error("[鉴权异常处理]请求路径:{}", exchange.getRequest().getPath());
        return webFluxResponseWriter(exchange.getResponse(), msg, ResultCode.FAILED_UNAUTHORIZED.getCode());
    }

    //拼装webflux模型响应
    private Mono<Void> webFluxResponseWriter(ServerHttpResponse response, String msg, int code) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        R<?> result = R.fail(code, msg);
        DataBuffer dataBuffer = response.bufferFactory().wrap(JSON.toJSONString(result).getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }

    @Override
    public int getOrder() {
        return -200;
    }

    public static void main(String[] args) {
        AuthFilter authFilter = new AuthFilter();
        // 测试 ?
        String pattern = "/sys/?bc";
        System.out.println(authFilter.isMatch(pattern,"/sys/abc"));
        System.out.println(authFilter.isMatch(pattern,"/sys/cbc"));
        System.out.println(authFilter.isMatch(pattern,"/sys/acbc"));
        System.out.println(authFilter.isMatch(pattern,"/sdsa/abc"));
        System.out.println(authFilter.isMatch(pattern,"/sys/abcw"));

         //测试*
         String pattern2 = "/sys/*/bc";
         System.out.println(authFilter.isMatch(pattern2,"/sys/a/bc"));
         System.out.println(authFilter.isMatch(pattern2,"/sys/sdasdsadsad/bc"));
         System.out.println(authFilter.isMatch(pattern2,"/sys/a/b/bc"));
         System.out.println(authFilter.isMatch(pattern2,"/a/b/bc"));
         System.out.println(authFilter.isMatch(pattern2,"/sys/a/b/"));

         //测试**
         String pattern3 = "/sys/**/bc";
         System.out.println(authFilter.isMatch(pattern3, "/sys/a/bc"));
         System.out.println(authFilter.isMatch(pattern3, "/sys/sdasdsadsad/bc"));
         System.out.println(authFilter.isMatch(pattern3, "/sys/a/b/bc"));
         System.out.println(authFilter.isMatch(pattern3, "/sys/a/b/s/23/432/fdsf///bc"));
         System.out.println(authFilter.isMatch(pattern3, "/a/b/s/23/432/fdsf///bc"));
         System.out.println(authFilter.isMatch(pattern3, "/sys/a/b/s/23/432/fdsf///"));
    }
}
