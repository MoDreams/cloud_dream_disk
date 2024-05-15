package com.cloud_disk.cloud_dream_disk.controller;

import com.cloud_disk.cloud_dream_disk.pojo.LoginInfo;
import com.cloud_disk.cloud_dream_disk.pojo.Result;
import com.cloud_disk.cloud_dream_disk.service.LoginInfoService;
import com.cloud_disk.cloud_dream_disk.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class LoginInfoController {
    @Autowired
    private LoginInfoService loginInfoService;

    //获取使用记录
    @GetMapping("/LoginRecords")
    public Result LoginRecords() {
        Map<String, String> map = ThreadLocalUtil.get();
        String userId = map.get("id");
        List<LoginInfo> loginInfo = loginInfoService.findLoginInfo(userId);
        if (loginInfo != null) return Result.success(loginInfo);
        return Result.error("出现未知错误,2032421,请联系管理人员");
    }

    //修改IP状态
    @PatchMapping("/UpdateStatus")
    public Result UpdateStatus(String Ip, Integer status) {
        if (status != 0 && status != 1) return Result.error("异常错误");
        loginInfoService.UpdateStatus(Ip, status);
        return Result.success();
    }
}
