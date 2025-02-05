package com.fast.jmx.config;

import com.fast.jmx.cache.FastJmxCache;
import com.fast.jmx.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            handleInvalidToken(response);
            return false;
        }

        token = token.replace("Bearer ", "");

        if (FastJmxCache.tokenMap.get(token) == null) {
            handleInvalidToken(response);
            return false;
        }

        // isTokenExpired 可能返回 null
        if (jwtTokenUtil.isTokenExpired(token)) {
            FastJmxCache.tokenMap.remove(token);
            rewriteResponse(response, "{\"code\":401,\"msg\":\"token 过期\"}");
            return false;
        }

        if (!jwtTokenUtil.validateToken(token, "admin")) {
            handleInvalidToken(response);
            return false;
        }

        return true;
    }

    private void handleInvalidToken(HttpServletResponse response) throws Exception {
        rewriteResponse(response, "{\"code\":401,\"msg\":\"token 无效\"}");
    }

    public void rewriteResponse(HttpServletResponse response, String content) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(content);
    }
}
