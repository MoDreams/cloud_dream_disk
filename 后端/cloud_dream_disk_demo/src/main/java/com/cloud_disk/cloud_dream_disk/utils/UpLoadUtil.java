package com.cloud_disk.cloud_dream_disk.utils;

import java.io.*;

public class UpLoadUtil {
    public static Boolean UpLoad(String url, String fileName, InputStream inputStream) throws IOException {
        Boolean CreateRes = FileOperationsUtil.CreateDirectory(url);
        if (!CreateRes) return false;

        FileOutputStream fileOutputStream = new FileOutputStream(url + "/" + fileName);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
        }

        fileOutputStream.close();
        inputStream.close();
        return true;
    }

}
