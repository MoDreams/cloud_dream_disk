package com.cloud_disk.cloud_dream_disk.pojo;

import org.springframework.web.multipart.MultipartFile;

public class FileChunk {
    private String identifier;//文件md5值
    private MultipartFile file;//分块文件
    private Integer chunkNumber;//分块序号
    private Integer chunkSize;//分块大小
    private Integer currentChunkSize;//当前分块大小
    private Long totalSize;//文件大小
    private Integer totalChunks;//分块数量
    private String filename;//文件名称
    private String url;//文件存储的路径
    private Integer lastShardSize;//最后一个分片的大小

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Integer getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(Integer chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Integer getCurrentChunkSize() {
        return currentChunkSize;
    }

    public void setCurrentChunkSize(Integer currentChunkSize) {
        this.currentChunkSize = currentChunkSize;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getLastShardSize() {
        return lastShardSize;
    }

    public void setLastShardSize(Integer lastShardSize) {
        this.lastShardSize = lastShardSize;
    }

    @Override
    public String toString() {
        return "FileChunk{" +
                "identifier='" + identifier + '\'' +
                ", file=" + file +
                ", chunkNumber=" + chunkNumber +
                ", chunkSize=" + chunkSize +
                ", currentChunkSize=" + currentChunkSize +
                ", totalSize=" + totalSize +
                ", totalChunks=" + totalChunks +
                ", filename='" + filename + '\'' +
                ", url='" + url + '\'' +
                ", lastShardSize=" + lastShardSize +
                '}';
    }
}
