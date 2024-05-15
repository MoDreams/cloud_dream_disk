package com.cloud_disk.cloud_dream_disk.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GetTimeUtil {
    /*
     * 获取当前时间
     * format: 时间格式("yyyy-MM-dd HH:mm:ss")
     */
    public static String GetNewTime(String format) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        String formattedDateTime = now.format(formatter);
        return formattedDateTime;
    }
}
