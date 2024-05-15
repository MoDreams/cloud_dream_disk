package com.cloud_disk.cloud_dream_disk.controller;

import com.cloud_disk.cloud_dream_disk.mapper.MyFileMapper;
import com.cloud_disk.cloud_dream_disk.pojo.FilesInfo;
import com.cloud_disk.cloud_dream_disk.pojo.Result;
import com.cloud_disk.cloud_dream_disk.pojo.ShardFile;
import com.cloud_disk.cloud_dream_disk.service.ShareFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class ShareFileController {
    @Autowired
    private ShareFileService shareFileService;
    //创建分享链接
    @PostMapping("/CreateShare")
    public Result CreateShare(String filepath,Integer time){
        if (time == 0){
            time = 1;
        }else if (time == 1){
            time = 10;
        }else if (time == 2){
            time = 30;
        } else if (time == 3) {
            time = 60;
        }else {
            return Result.error("参数出现异常");
        }
        String SharePath = shareFileService.createShare(filepath,time);
        return Result.success(SharePath);
    }
    //浏览分享链接
    @GetMapping("/list")
    public Result list(String path){
        ShardFile shardFile = shareFileService.list(path);
        if (LocalDateTime.now().isBefore(shardFile.getLastTime())){//如果当前时间小于过期时间
            Map<String, Object> filesInfo = shareFileService.FileList(shardFile);
            return Result.success(filesInfo);
        }
        return Result.error("该分享的文件已过期");
    }
}
