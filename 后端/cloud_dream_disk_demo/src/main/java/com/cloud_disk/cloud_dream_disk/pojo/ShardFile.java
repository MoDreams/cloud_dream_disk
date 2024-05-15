package com.cloud_disk.cloud_dream_disk.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class ShardFile {
    private String UserId;
    private String Md5Path;
    private String FilePath;
    private String DownloadNum;
    private String Browse;
    private LocalDateTime CreateTime;
    private LocalDateTime LastTime;

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getMd5Path() {
        return Md5Path;
    }

    public void setMd5Path(String md5Path) {
        Md5Path = md5Path;
    }

    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }

    public String getDownloadNum() {
        return DownloadNum;
    }

    public void setDownloadNum(String downloadNum) {
        DownloadNum = downloadNum;
    }

    public String getBrowse() {
        return Browse;
    }

    public void setBrowse(String browse) {
        Browse = browse;
    }

    public LocalDateTime getCreateTime() {
        return CreateTime;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public void setCreateTime(LocalDateTime createTime) {
        CreateTime = createTime;
    }

    public LocalDateTime getLastTime() {
        return LastTime;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public void setLastTime(LocalDateTime lastTime) {
        LastTime = lastTime;
    }

    @Override
    public String toString() {
        return "ShardFile{" +
                "UserId='" + UserId + '\'' +
                ", Md5Path='" + Md5Path + '\'' +
                ", FilePath='" + FilePath + '\'' +
                ", DownloadNum='" + DownloadNum + '\'' +
                ", Browse='" + Browse + '\'' +
                ", CreateTime='" + CreateTime + '\'' +
                ", LastTime='" + LastTime + '\'' +
                '}';
    }
}
