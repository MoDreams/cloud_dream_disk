package com.cloud_disk.cloud_dream_disk.service;

import com.cloud_disk.cloud_dream_disk.pojo.FilesInfo;
import com.cloud_disk.cloud_dream_disk.pojo.ShardFile;

import java.util.Map;

public interface ShareFileService {
    String createShare(String filepath,Integer time);

    ShardFile list(String md5Path);

    Map<String, Object> FileList(ShardFile shardFile);
}
