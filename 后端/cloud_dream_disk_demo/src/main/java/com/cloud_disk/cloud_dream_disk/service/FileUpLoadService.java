package com.cloud_disk.cloud_dream_disk.service;

import com.cloud_disk.cloud_dream_disk.pojo.FileChunk;
import com.cloud_disk.cloud_dream_disk.pojo.FileChunkResult;
import com.cloud_disk.cloud_dream_disk.pojo.FilesInfo;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface FileUpLoadService {
    FileChunkResult checkChunkExist(FileChunk fileChunk);

    void uploadChunk(FileChunk fileChunk) throws IOException;

    List<Integer> mergeChunks(FileChunk fileChunk);

    Boolean SelectFile(String fileMd5,String url,String fileName) throws IOException;

    List<Map<String, Object>> ShardDetails(ArrayList<Map<String,String>> list);

    Map<String,Object> Download(Integer shardNum, String md5, String path) throws IOException;

    ResponseEntity<Resource> RespDownload(File file) throws UnsupportedEncodingException;

    String loadPic(String url, Integer type) throws FileNotFoundException;
}
