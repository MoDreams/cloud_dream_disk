package com.cloud_disk.cloud_dream_disk.interceptors;

import com.cloud_disk.cloud_dream_disk.utils.JWTUtil;
import com.cloud_disk.cloud_dream_disk.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class GlobalInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        try {
            String res = operations.get(token);
            if (res == null) throw new RuntimeException();

            Map<String, Object> map = JWTUtil.ParamsToken(token);
            ThreadLocalUtil.set(map);

            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
