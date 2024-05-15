package com.cloud_disk.cloud_dream_disk.utils;

import jakarta.servlet.http.HttpServletRequest;

public class GetDeviceInfoUtil {
    public static String getDeviceInfo(HttpServletRequest request) {
        String deviceName = null;
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "非法请求,数据不存在";
        if (userAgent != null && !userAgent.isEmpty()) {
            if (userAgent.contains("iPhone")) {
                deviceName = "iPhone";
            } else if (userAgent.contains("Android")) {
                deviceName = "Android";
            } else if (userAgent.contains("Windows NT")) {
                deviceName = "Windows";
            } else if (userAgent.contains("Macintosh")) {
                deviceName = "Mac";
            }
        }

        return deviceName;
    }
}
