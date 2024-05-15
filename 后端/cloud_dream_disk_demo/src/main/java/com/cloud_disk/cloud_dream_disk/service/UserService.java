package com.cloud_disk.cloud_dream_disk.service;

import com.cloud_disk.cloud_dream_disk.pojo.LoginInfo;
import com.cloud_disk.cloud_dream_disk.pojo.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    //查询是否存在该用户名
    User findByUserEmail(String Email);
    //注册用户
    String register(String username, String password);

    void AddLoginInfo(String userCardID, String ip, String device);

    void RestPassword(String md5String,String Email);

    User findMyInfo(String id);

    void updateLoginInfo(String userCardID, String ip, String device);

    void UpdateInfo(User user);

    void updateAvatar(String newPicUrl);

}
