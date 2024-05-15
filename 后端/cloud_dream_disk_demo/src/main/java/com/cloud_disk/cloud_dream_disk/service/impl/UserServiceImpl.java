package com.cloud_disk.cloud_dream_disk.service.impl;


import com.cloud_disk.cloud_dream_disk.mapper.UserMapper;
import com.cloud_disk.cloud_dream_disk.pojo.LoginInfo;
import com.cloud_disk.cloud_dream_disk.pojo.User;
import com.cloud_disk.cloud_dream_disk.service.UserService;
import com.cloud_disk.cloud_dream_disk.utils.Md5Util;
import com.cloud_disk.cloud_dream_disk.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public User findByUserEmail(String Email) {
        User u = userMapper.findByUserEmail(Email);
        return u;
    }

    @Override
    public String register(String Email, String password) {

        String md5String = Md5Util.getMD5String(Md5Util.getMD5String(password) + "Dream");
        //24 17 0928 447 3 9460
        String TimeStamp = String.valueOf(System.currentTimeMillis());
        String CardId = "24" + TimeStamp.substring(0,9) + "3" + TimeStamp.substring(9,13);

        userMapper.register(Email,md5String,CardId);
        return CardId;
    }

    @Override
    public void AddLoginInfo(String userCardID, String ip, String device) {
        userMapper.AddLoginInfo(userCardID,ip,device);
    }

    @Override
    public void RestPassword(String md5String,String Email) {
        userMapper.RestPassword(md5String,Email);
    }

    @Override
    public User findMyInfo(String id) {
        User u = userMapper.findMyInfo(id);
//        System.out.println(u);
        return u;
    }

    @Override
    public void updateLoginInfo(String userCardID, String ip, String device) {
        userMapper.updateLoginInfo(userCardID, ip, device);
    }

    @Override
    public void UpdateInfo(User user) {
        Map<String,Object> map = ThreadLocalUtil.get();
        user.setUserCardID((String) map.get("id"));
        userMapper.UpdateInfo(user);
    }

    @Override
    public void updateAvatar(String newPicUrl) {
        Map<String,Object> map = ThreadLocalUtil.get();
        String userId = (String) map.get("id");
        userMapper.updateAvatar(newPicUrl,userId);
    }

}
