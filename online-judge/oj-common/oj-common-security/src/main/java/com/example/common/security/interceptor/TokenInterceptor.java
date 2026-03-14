package com.example.common.security.interceptor;

import cn.hutool.core.util.StrUtil;
import com.example.common.core.constants.HttpConstants;
import com.example.common.security.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    //从哪个服务的配置文件中去读取，bean会交给哪个服务的spring容器去管理
    @Value("${jwt.secret}")
    private String secret;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = getToken(request);
        if(StrUtil.isEmpty(token)){
            return true;
        }
        //token有效时间的延长
        tokenService.extendToken(token,secret);
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
}
