package com.cloud_disk.cloud_dream_disk.service;

import com.cloud_disk.cloud_dream_disk.pojo.FilesInfo;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MyFilesService {
    void deleteFile(List<String> fileUrl, String userId) throws IOException, NoSuchAlgorithmException;

    void moveUpdate(String userId,String url, String moveUrl);

    List selectState(List<Map<String, Object>> list,Integer del) throws IOException;

    void reclaim(String url, String userId,Integer del_flag);

    List<Integer> GetFilesNum(Integer type);

    List<List<Integer>> GetWeekFilesNum();

    void add(FilesInfo fileInfo);

    List<Map<String, Object>> SelectReclaim(String userId);

    List<Map<String, Object>> FindFile(String name,Integer type);
}
