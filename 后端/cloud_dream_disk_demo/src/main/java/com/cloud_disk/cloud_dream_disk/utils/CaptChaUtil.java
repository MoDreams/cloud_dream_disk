package com.cloud_disk.cloud_dream_disk.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
@Component
public class CaptChaUtil {
    //基础文字
    private String words = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Random random = new Random();
    public Map<String,String> Pic() throws IOException {
        Map<String,String> map = new HashMap<>();
        int width = 200;
        int height = 75;
        //验证码字符生成数量
        int count = 4;
        int word_len = words.length();
        //生成干扰点个数
        int points = 400;
        // 生成干扰线的个数
        int lineCount = 5;
        StringBuffer stringBuffer = new StringBuffer();
        //创建画板
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //获取笔触
        Graphics gs = image.getGraphics();
        // 设置笔触颜色
        gs.setColor(Color.WHITE);
        //填充
        gs.fillRect(0, 0, width, height);

        //生成验证字符
        for (int i = 0; i < count; i++) {
            //设置字体样式
            gs.setFont(new Font("楷体", Font.BOLD + Font.ITALIC, random.nextInt(30) + 25));
            gs.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            char Captcha = words.charAt(random.nextInt(word_len));
            stringBuffer.append(Captcha);
            //绘制文字
            gs.drawString(String.valueOf(Captcha), 40 + (i * 30), (height - 20) + random.nextInt(10));
        }
        System.out.println("验证码：" + stringBuffer);
        String CaptCha = stringBuffer.toString().toUpperCase();
        map.put("CaptCha", CaptCha);
        //绘制干扰
        //干扰点
        gs.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < points; i++) {
            gs.drawOval(random.nextInt(width), random.nextInt(height), 1, 1);
        }
        //线条
        // 干扰线
        Graphics2D g2d = (Graphics2D) gs;//将gs转换成Graphics2D对象
        float strokeWidth = 2.0f; // 设置笔触宽度为3像素
        g2d.setStroke(new BasicStroke(strokeWidth)); // 设置笔触宽度
        for (int i = 0; i < lineCount; i++) {
            // 生成随机起点坐标
            int startX = random.nextInt(width / 2);
            int startY = random.nextInt(height / 2);
            // 生成随机终点坐标
            int endX = random.nextInt(width);
            int endY = random.nextInt(height);
            // 设置画笔颜色
            g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            // 绘制干扰线
            g2d.drawLine(startX, startY, endX, endY);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //将image图像以png形式写入字节数组输出流中
        ImageIO.write(image, "png", byteArrayOutputStream);
        //字节数组输出流转换成字节数组
        byte[] bytes = byteArrayOutputStream.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(bytes);
        map.put("Img",base64Image);
        return map;
    }
}