package com.cloud_disk.cloud_dream_disk;

import com.cloud_disk.cloud_dream_disk.pojo.User;
import com.cloud_disk.cloud_dream_disk.service.UserService;
import com.cloud_disk.cloud_dream_disk.service.impl.MyFilesServiceImpl;
import com.cloud_disk.cloud_dream_disk.utils.FileOperationsUtil;
import com.cloud_disk.cloud_dream_disk.utils.JWTUtil;
import com.cloud_disk.cloud_dream_disk.utils.SizeConversionUtil;
import com.cloud_disk.cloud_dream_disk.utils.UpLoadUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@SpringBootTest
public class test {
    @Autowired
    private UserService userService;

    @Test
    public void mails() {
        StringBuffer code = new StringBuffer();
        Random random = new Random();
        String works = "1234567890ABCDEFGHIJKLMNOPQRSTUVWSYZ";
        for (int i = 0; i < 6; i++) {
            code.append(works.charAt(random.nextInt(works.length() - 1)));
        }
        System.out.println(code);
    }

    @Test
    public void id() {
        long timeStamp = System.currentTimeMillis();
        System.out.println(timeStamp);

        String TimeStamp = String.valueOf(System.currentTimeMillis());
        String CardId = "24" + TimeStamp.substring(0, 9) + "3" + TimeStamp.substring(9, 13);
        System.out.println(CardId);
        //24 17 0928 5063 8780
        //24 17 0928 5063 8780

    }

    @Test
    public void testJWT() {
        Map<String, Object> map = new HashMap<>();
        User u = userService.findByUserEmail("125361@qq.com");

        map.put("id", u.getUserCardID());
        map.put("username", u.getUsername());
        map.put("status", (int) u.getStatus());
        map.put("permissions", (int) u.getPermissions());
        System.out.println(map);
        String Token = JWTUtil.GetToken(map);

        System.out.println(Token);

    }

    @Test
    public void list() throws IOException, NoSuchAlgorithmException {
        File file = new File("D:\\HuaweiMoveData\\Users\\沫梦\\Desktop\\附件");
        File[] files = file.listFiles();
        List<Map<String, Object>> list = new ArrayList<>();

        for (File file1 : files) {
            String md5 = MyFilesServiceImpl.getFileMD5(file1);
            System.out.println(file1.toString() + " 的MD5值: " + md5);
        }
        System.out.println(list);
    }

    @Test
    public void Time() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime plus30Days = now.plusDays(30);

        System.out.println("当前日期时间: " + now);
        System.out.println("30天后的日期时间: " + plus30Days);
    }
    //视频第一帧图片读取测试
    @Test
    public void ffmpegVideo() {
        String url = "D:/Cloud_dream_disk/storage/2417120422039612/20230921_235929.mp4";
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);
        try{
            grabber.start();
            //读取第一帧
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage firstImg = converter.convert(grabber.grabImage());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(firstImg,"jpg",byteArrayOutputStream);

            System.out.println(Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            grabber.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //图片压缩测试
    @Test
    public  void test() throws FileNotFoundException {
        String url = "D:\\HuaweiMoveData\\Users\\沫梦\\Desktop\\OIP-C.jpg";
        FileInputStream fileInputStream = new FileInputStream(url);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(fileInputStream).scale(1.00f)
                    .outputQuality(0.15f)                       //压缩质量 范围（0.00--1.00）
                    .toOutputStream(out);
            System.out.println(Base64.getEncoder().encodeToString(out.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
