package com.cloud_disk.cloud_dream_disk.utils;

import com.cloud_disk.cloud_dream_disk.pojo.Result;
import com.cloud_disk.cloud_dream_disk.service.FileUpLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileOperationsUtil {
    public static Boolean CreateDirectory(String url) {
        File directory = new File(url);
        if (!directory.exists()) {
            Boolean res = directory.mkdirs();
            if (res) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public static Boolean DeleteDirectory(String url) {
        File folder = new File(url);
        if (folder.exists()) {
            Boolean res = folder.delete();
            if (res) {
                return true;
            } else {
                return false;
            }
        }
        return true;//文件、文件夹不存在,和删除差不多,都是没了
    }

    public static List<String> DeleteFolderRecursively(File folder) {
        File[] files = folder.listFiles();
        List<String> list = new ArrayList<>();
        try {
            for (File file : files) {
                if (file.isDirectory()) {
                    list.addAll(DeleteFolderRecursively(file));
                } else {
                    file.delete();
                }
//                System.out.println(file);
                list.add(file.toString().replace("BY沫梦[回收站&标识符]1710836750184","").replace("\\","/"));
            }
            folder.delete();
        } catch (Exception e) {
            System.out.println("DeleteFolder" + e.getMessage());
            return null;
        }
        return list;
    }

    //修改文件夹名称
    public static Boolean ReviseFolderName(String url, String ReviseUrl) {
        File file = new File(ReviseUrl);
        if (file.exists()) return false;
        File file1 = new File(url);
        if (file1.exists()) {
            file1.renameTo(file);
            return true;
        }
        return false;
    }

    //查看文件夹内容
    public static List<Map<String, Object>> ViewDirectory(String url, Integer del) {
        File file = new File(url);
        if (!file.exists()) return null;
        File[] files = file.listFiles();
        List<Map<String, Object>> list = new ArrayList<>();

        for (File file1 : files) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", file1.toString());
            if (file1.isFile()) {
                map.put("type", 0);
                if (file1.length() == 0) {
                    map.put("size", "0 kb");
                } else {
                    map.put("size", SizeConversionUtil.ByteAutomaticConversion((double) file1.length()));
                }
            } else if (file1.isDirectory()) {
                map.put("type", 1);
                map.put("size", "-");
            }
            map.put("path", "");
            map.put("createTime", "");
            map.put("updateTime", "");
            map.put("category", "");
            map.put("md5","");
            if (del == 1) {
                map.put("recoveryTime", "");
                map.put("deleteTime", "");
            }
            list.add(map);
        }
        return list;
    }

    //移动文件夹
    public static Boolean MoveFile(String Url, String NewUrl) throws IOException {
        File file = new File(Url);
        File file1 = new File(NewUrl);
        if (file.exists() && file1.exists()) {
            Files.move(Path.of(Url), Path.of(NewUrl), StandardCopyOption.REPLACE_EXISTING);
            return true;
        }
        return false;
    }
    //复制文件
    public  static Boolean CopyFile(String url,String NewUrl) {
        Path oldUrl = Paths.get(url);
        Path newUrl = Paths.get(NewUrl);
        try {
            Files.copy(oldUrl,newUrl);
            return true;
        } catch (IOException e) {
            System.out.println("复制异常:" + e.getMessage());
            return false;
        }

    }


    //容量计算
    public static Long UseSpace(String url) {
        Long Space = 0L;//= 0.0;
        File file = new File(url);
        for (File file1 : file.listFiles()) {
            if (file1.isFile()) {
                Space += file1.length();
            } else {
                Space += UseSpace(file1.toString());
            }
        }
        return Space;
    }

}
