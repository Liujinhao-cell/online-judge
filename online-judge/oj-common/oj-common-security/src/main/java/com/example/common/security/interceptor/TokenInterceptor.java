package com.example.common.security.interceptor;

import cn.hutool.core.util.StrUtil;
import com.example.common.core.constants.Constants;
import com.example.common.core.constants.HttpConstants;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.common.security.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    //从哪个服务的配置文件中去读取，bean会交给哪个服务的spring容器去管理
    @Value("${jwt.secret}")
    private String secret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("========== TokenInterceptor 执行 ==========");
        String token = getToken(request);
        if (StrUtil.isEmpty(token)) {
            return true;
        }
        //存储userid
        Claims claims = tokenService.getClaims(token, secret);
        Long userId = tokenService.getUserId(claims);
        String userKey = tokenService.getUserKey(token, secret);
        ThreadLocalUtil.set(Constants.USER_ID, userId);
        ThreadLocalUtil.set(Constants.USER_KEY, userKey);
        //token有效时间的延长
        tokenService.extendToken(token, secret);
        //放行
        return true;
    }

    /**
     * 从请求头中获取请求token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(HttpConstants.AUTHENTICATION);
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return token;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
