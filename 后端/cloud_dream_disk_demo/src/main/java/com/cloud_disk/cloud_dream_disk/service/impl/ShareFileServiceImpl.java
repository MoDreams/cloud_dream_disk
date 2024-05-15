package com.cloud_disk.cloud_dream_disk.service.impl;

import com.cloud_disk.cloud_dream_disk.mapper.MyFileMapper;
import com.cloud_disk.cloud_dream_disk.mapper.ShareFileMapper;
import com.cloud_disk.cloud_dream_disk.pojo.FilesInfo;
import com.cloud_disk.cloud_dream_disk.pojo.ShardFile;
import com.cloud_disk.cloud_dream_disk.service.ShareFileService;
import com.cloud_disk.cloud_dream_disk.utils.Md5Util;
import com.cloud_disk.cloud_dream_disk.utils.SizeConversionUtil;
import com.cloud_disk.cloud_dream_disk.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ShareFileServiceImpl implements ShareFileService {
    @Autowired
    private ShareFileMapper shareFileMapper;
    @Autowired
    private MyFileMapper myFileMapper;
    //文件存储路径
    @Value("${storage.FileRootUrl}")
    private String FileRootUrl;

    @Override
    public String createShare(String filepath, Integer time) {
        Map<String, String> map = ThreadLocalUtil.get();
        String userId = map.get("id");
        String Md5Path = Md5Util.getMD5String(filepath + "沫梦_Dream" + System.currentTimeMillis());
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime plus30Days = currentTime.plusDays(time);
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ShardFile shardFile = new ShardFile();
        shardFile.setUserId(userId);
        shardFile.setMd5Path(Md5Path);
        filepath = FileRootUrl + userId + filepath;
        shardFile.setFilePath(filepath);
        shardFile.setCreateTime(LocalDateTime.parse(currentTime.format(dateTimeFormat), dateTimeFormat));
        shardFile.setLastTime(LocalDateTime.parse(plus30Days.format(dateTimeFormat), dateTimeFormat));
        shareFileMapper.createShare(shardFile);
        return Md5Path;
    }

    @Override
    public ShardFile list(String md5Path) {
        ShardFile shardFile = shareFileMapper.list(md5Path);
        return shardFile;
    }

    @Override
    public Map<String, Object> FileList(ShardFile shardFile) {
        String FilePath = shardFile.getFilePath();
        FilesInfo filesInfo = myFileMapper.selectState(FilePath, 0, shardFile.getUserId());
        Map<String, Object> NewMap = new HashMap<>();
        NewMap.put("name", filesInfo.getFileName());
        NewMap.put("md5", filesInfo.getFileMd5());
        String path = filesInfo.getFilePath().substring(44, (filesInfo.getFilePath().length() - 1) - (filesInfo.getFileName().length() - 1));
        NewMap.put("path", path);
        File file = new File(filesInfo.getFilePath());
        NewMap.put("size", SizeConversionUtil.ByteAutomaticConversion((double) file.length()));
        NewMap.put("createTime",filesInfo.getCreateTime());
        NewMap.put("updateTime",filesInfo.getLastUpdateTime());
        NewMap.put("type",filesInfo.getFolderType());
        NewMap.put("category",filesInfo.getFileCategory());
        return NewMap;
    }
}
