package com.cloud_disk.cloud_dream_disk.service;

import com.cloud_disk.cloud_dream_disk.pojo.LoginInfo;

import java.util.List;

public interface LoginInfoService {
    List<LoginInfo> findLoginInfo(String userId);

    Boolean IpSelect(String ip,String userId);

    void UpdateStatus(String ip, Integer status);
}
