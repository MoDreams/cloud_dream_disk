package com.cloud_disk.cloud_dream_disk.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileChunkResult {
    private Boolean skipUpload;//是否跳过上传
    private List<Integer> uploaded;//已上传分片集合

    public Boolean getSkipUpload() {
        return skipUpload;
    }

    public void setSkipUpload(Boolean skipUpload) {
        this.skipUpload = skipUpload;
    }

    public List<Integer> getUploaded() {
        return uploaded;
    }

    public void setUploaded(List<Integer> uploaded) {
        this.uploaded = uploaded;
    }

    @Override
    public String toString() {
        return "FileChunkResult{" +
                "skipUpload=" + skipUpload +
                ", uploaded=" + uploaded +
                '}';
    }
}
