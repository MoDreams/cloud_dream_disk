package com.cloud_disk.cloud_dream_disk.service.impl;

import com.cloud_disk.cloud_dream_disk.mapper.LoginInfoMapper;
import com.cloud_disk.cloud_dream_disk.pojo.LoginInfo;
import com.cloud_disk.cloud_dream_disk.service.LoginInfoService;
import com.cloud_disk.cloud_dream_disk.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Service
public class LoginInfoServiceImpl implements LoginInfoService {
    @Autowired
    private LoginInfoMapper loginInfoMapper;
    @Override
    public List<LoginInfo> findLoginInfo(String userCardID) {
        List<LoginInfo> loginInfo = loginInfoMapper.findLoginInfo(userCardID);
        return loginInfo;
    }

    @Override
    public Boolean IpSelect(String ip,String userId) {
        //同IP不同登录设备
        List<LoginInfo> loginInfo = loginInfoMapper.findInfo(ip,userId);
        for (int i=0;i<loginInfo.size();i++){
            if (loginInfo.get(i).getStatus() == 1){
                return false;
            }
        }
        return true;
    }

    @Override
    public void UpdateStatus(String ip, Integer status) {
        Map<String,String> map = ThreadLocalUtil.get();
        String userId = map.get("id");
        loginInfoMapper.UpdateStatus(ip,status,userId);
    }
}
