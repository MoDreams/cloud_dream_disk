package com.cloud_disk.cloud_dream_disk.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

public class ReadImage {
    public static String ImageToBase64(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bytes = new byte[(int) file.length()];
                int read = fileInputStream.read(bytes);
                fileInputStream.close();
                if (read == bytes.length) {
                    return Base64.getEncoder().encodeToString(bytes);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
