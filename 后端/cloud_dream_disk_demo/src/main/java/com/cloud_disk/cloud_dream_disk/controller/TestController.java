package com.cloud_disk.cloud_dream_disk.controller;

import com.cloud_disk.cloud_dream_disk.pojo.Result;
import com.cloud_disk.cloud_dream_disk.utils.GetDeviceInfoUtil;
import com.cloud_disk.cloud_dream_disk.utils.GetIpAddressUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public Result test(HttpServletRequest request){
        String ip = GetIpAddressUtil.getIpAddr(request);
        String Device = GetDeviceInfoUtil.getDeviceInfo(request);
        return Result.success(Device);
    }
}
