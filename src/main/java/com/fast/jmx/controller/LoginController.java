package com.fast.jmx.controller;

import com.fast.jmx.cache.FastJmxCache;
import com.fast.jmx.config.FastJmxProperties;
import com.fast.jmx.domain.R;
import com.fast.jmx.domain.User;
import com.fast.jmx.utils.FastJmxUtils;
import com.fast.jmx.utils.JwtTokenUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/")
public class LoginController {

    @Resource
    private FastJmxProperties fastJmxProperties;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public R login(@RequestBody User user) {
        if (user.getUsername().equals(fastJmxProperties.getUsername()) && user.getPassword().equals(fastJmxProperties.getPassword())) {
            String token = jwtTokenUtil.generateToken(user.getUsername());
            FastJmxCache.tokenMap.put(token, user.getUsername());
            return R.ok("登录成功", token);
        }
        return R.error("用户名或密码错误");
    }

    @GetMapping("/logout")
    public R logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            return R.error("未找到token");
        }
        String token = authorization.replace("Bearer ", "");
        FastJmxCache.tokenMap.remove(token);
        return R.ok("退出成功");
    }
}
