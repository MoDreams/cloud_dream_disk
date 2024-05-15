package com.cloud_disk.cloud_dream_disk.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class JWTUtil {
    private static final String KEY = "By Dream_2024 H";

    public static String GetToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 6))
                .sign(Algorithm.HMAC256(KEY)).toString();
    }

    public static Map<String, Object> ParamsToken(String token) {
        return JWT.require(Algorithm.HMAC256(KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

}
