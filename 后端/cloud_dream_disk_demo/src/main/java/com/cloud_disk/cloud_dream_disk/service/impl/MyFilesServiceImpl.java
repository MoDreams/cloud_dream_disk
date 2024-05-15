package com.cloud_disk.cloud_dream_disk.service.impl;

import com.cloud_disk.cloud_dream_disk.mapper.FileInfoMapper;
import com.cloud_disk.cloud_dream_disk.mapper.MyFileMapper;
import com.cloud_disk.cloud_dream_disk.pojo.FilesInfo;
import com.cloud_disk.cloud_dream_disk.pojo.User;
import com.cloud_disk.cloud_dream_disk.service.MyFilesService;
import com.cloud_disk.cloud_dream_disk.utils.AnomalyDetails;
import com.cloud_disk.cloud_dream_disk.utils.SizeConversionUtil;
import com.cloud_disk.cloud_dream_disk.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MyFilesServiceImpl implements MyFilesService {
    @Autowired
    private MyFileMapper myFileMapper;
    @Autowired
    private FileInfoMapper filesInfoMapper;

    @Override
    public void deleteFile(List<String> fileUrl, String userId) {
        for (int i = 0; i < fileUrl.size(); i++) {
//            myFileMapper.deleteFile(fileMd5s.get(i),userId);
//            File file = new File(fileUrl.get(i));
//            String FileMd5 = getFileMD5(file);
            System.out.println(fileUrl.get(i) + " | " + userId);
            myFileMapper.deleteFile(fileUrl.get(i), userId);
        }
    }

    @Override
    public void moveUpdate(String userId, String url, String moveUrl) {
        String FileName = moveUrl.split("/")[moveUrl.split("/").length - 1];
//        System.out.println(FileName + " " + userId + " " + url + " " + moveUrl);
        myFileMapper.moveUpdate(userId, url, moveUrl, FileName);
    }

    @Override
    public List selectState(List<Map<String, Object>> list, Integer del) throws IOException {
        Map<String, String> map = ThreadLocalUtil.get();
        String userId = map.get("id");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String Del_Str = "BY沫梦[回收站&标识符]1710836750184";
//        try {
            for (int i = 0; i < list.size(); i++) {
                String url = list.get(i).get("name").toString().replace("\\", "/");
                FilesInfo filesInfo = myFileMapper.selectState(url, del, userId);
                if (filesInfo == null) {
                    list.remove(i);
                    i--;
                    continue;
                }
                list.get(i).put("createTime", filesInfo.getCreateTime().format(formatter));
                list.get(i).put("updateTime", filesInfo.getLastUpdateTime().format(formatter));
                list.get(i).put("category", filesInfo.getFileCategory());
                list.get(i).put("md5", filesInfo.getFileMd5());
                String name = url.split("/")[url.split("/").length - 1];
                String path = url.substring(44, (url.length() - 1) - (name.length() - 1));
                if (del == 1) {
                    list.get(i).put("name", name.replace(Del_Str, ""));
                    list.get(i).put("recoveryTime", filesInfo.getRecoveryTime().format(formatter));
                    LocalDateTime after30Days = filesInfo.getRecoveryTime().plus(30, ChronoUnit.DAYS);
                    list.get(i).put("deleteTime", after30Days.format(formatter));

                } else {
                    list.get(i).put("name", name);
                }
                list.get(i).put("path", path);
            }
//        } catch (Exception e) {
//            System.out.println("Impl查看列表异常" + e.getMessage());
//            System.out.println(AnomalyDetails.getExceptionDetail(e));
//            System.out.println(AnomalyDetails.getThrowableDetail(e));
//        }

        return list;
    }

    @Override
    public List<Map<String, Object>> SelectReclaim(String userId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String Del_Str = "BY沫梦[回收站&标识符]1710836750184";
        List<Map<String, Object>> list = new ArrayList<>();
        List<FilesInfo> list1 = myFileMapper.SelectReclaim(userId);
        for (int i = 0; i < list1.size(); i++) {
            Map<String, Object> Newmap = new HashMap<>();
            FilesInfo filesInfo = list1.get(i);
            Newmap.put("name", filesInfo.getFileName().replace(Del_Str, ""));
            Integer ida = filesInfo.getFilePath().lastIndexOf("/");
            Newmap.put("path", filesInfo.getFilePath().substring(44, ida + 1));
            Newmap.put("size", "-");
            Newmap.put("type", "-");
            Newmap.put("createTime", filesInfo.getCreateTime().format(formatter));
            Newmap.put("updateTime", filesInfo.getLastUpdateTime().format(formatter));
            Newmap.put("category", filesInfo.getFileCategory());
            Newmap.put("recoveryTime", filesInfo.getRecoveryTime().format(formatter));
            LocalDateTime after30Days = filesInfo.getRecoveryTime().plus(30, ChronoUnit.DAYS);
            Newmap.put("deleteTime", after30Days.format(formatter));
            list.add(Newmap);
        }

        return list;
    }

    @Override
    public List<Map<String, Object>> FindFile(String name, Integer type) {
        Map<String, String> map = ThreadLocalUtil.get();
        String UserId = map.get("id");
        List<FilesInfo> list = new ArrayList<>();
        if (type == 0) list = myFileMapper.FindFile(name, UserId);
        if (type >= 1 && type <= 5) list = myFileMapper.FindCategory(UserId, type);
        List list1 = new ArrayList<>();
        if (type < 0 || type > 5) return list1;
        for (int i = 0; i < list.size(); i++) {
            FilesInfo filesInfo = list.get(i);
            Map<String, Object> Map1 = new HashMap<>();
            Map1.put("name", filesInfo.getFileName());
            Map1.put("md5", filesInfo.getFileMd5());
            String path = filesInfo.getFilePath().substring(44, (filesInfo.getFilePath().length() - 1) - (filesInfo.getFileName().length() - 1));
            Map1.put("path", path);
            File file = new File(filesInfo.getFilePath());
            Map1.put("size", SizeConversionUtil.ByteAutomaticConversion((double) file.length()));
            Map1.put("createTime", filesInfo.getCreateTime());
            Map1.put("updateTime", filesInfo.getLastUpdateTime());
            Map1.put("type", filesInfo.getFolderType());
            Map1.put("category", filesInfo.getFileCategory());
            list1.add(Map1);
        }
        return list1;
    }

    @Override
    public void reclaim(String url, String userId, Integer del_flag) {
        LocalDateTime recovery_time = null;
        if (del_flag == 1) {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            recovery_time = LocalDateTime.parse(currentTime.format(dateTimeFormat), dateTimeFormat);
        }
        System.out.println(del_flag);
        System.out.println(url);
        try {
            myFileMapper.reclaim(url, userId, del_flag, recovery_time);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Integer> GetFilesNum(Integer type) {
        Map<String, String> map = ThreadLocalUtil.get();
        List<Integer> list = new ArrayList<>();
        String userId = map.get("id");
        if (type == 0) {
            list.add(myFileMapper.GetFilesNum(userId, 1));
            list.add(myFileMapper.GetFilesNum(userId, 3));
            list.add(myFileMapper.GetFilesNum(userId, 4));
            list.add(myFileMapper.GetFilesNum(userId, 5));
        }
        return list;
    }

    @Override
    public List<List<Integer>> GetWeekFilesNum() {
        Map<String, String> map = ThreadLocalUtil.get();
        String userId = map.get("id");
        List<String> time = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        List<List<Integer>> lists = new ArrayList<>();
        //获取一周日期
        LocalDate today = LocalDate.now();
        //2024-03-18
        time.add(String.valueOf(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))));
        time.add(String.valueOf(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.TUESDAY))));
        time.add(String.valueOf(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY))));
        time.add(String.valueOf(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.THURSDAY))));
        time.add(String.valueOf(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY))));
        time.add(String.valueOf(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))));
        time.add(String.valueOf(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))));
        for (int i = 1; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                list.add(myFileMapper.GetWeekFilesNum(userId, time.get(j) + " 00:00:00", time.get(j) + " 23:59:59", i));
            }
            lists.add(new ArrayList<>(list));
            list.clear();
        }
        return lists;
    }

    @Override
    public void add(FilesInfo fileInfo) {
        filesInfoMapper.add(fileInfo);
    }


    public static String getFileMD5(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fileChannel = fis.getChannel()) {
            long fileSize = fileChannel.size();
            long bufferSize = 1024 * 1024; // 1MB
            long position = 0;
            while (position < fileSize) {
                long remaining = fileSize - position;
                int size = (int) Math.min(bufferSize, remaining);
                MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, position, size);
                messageDigest.update(mappedByteBuffer);
                position += size;
            }
        }
        byte[] digest = messageDigest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
