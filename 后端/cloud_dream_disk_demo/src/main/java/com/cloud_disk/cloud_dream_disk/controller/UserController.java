package com.cloud_disk.cloud_dream_disk.controller;

import com.cloud_disk.cloud_dream_disk.pojo.LoginInfo;
import com.cloud_disk.cloud_dream_disk.pojo.Result;
import com.cloud_disk.cloud_dream_disk.pojo.User;
import com.cloud_disk.cloud_dream_disk.service.LoginInfoService;
import com.cloud_disk.cloud_dream_disk.service.UserService;
import com.cloud_disk.cloud_dream_disk.utils.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.OnClose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private LoginInfoService loginInfoService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CaptChaUtil captChaUtil;
    @Autowired
    private SendMailUtil sendMailUtil;

    //获取图片验证码
    @GetMapping("/CaptCha")
    public Result CaptCha() throws IOException {
        Map<String, String> map = captChaUtil.Pic();
        String CaptCha = map.get("CaptCha");
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set(String.valueOf(CaptCha), String.valueOf(CaptCha), 1, TimeUnit.MINUTES);
        return Result.success(map.get("Img"));
    }

    //发送邮箱验证码
    @PostMapping("/ECaptCha")
    public Result ECaptCha(String Email, String CaptCha_Pic, String Type) throws MessagingException {
        System.out.println(CaptCha_Pic);
        if (Email.equals("") || CaptCha_Pic.equals("")) return Result.error("参数为空");
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        CaptCha_Pic = CaptCha_Pic.toUpperCase();
        String Verify = operations.get(CaptCha_Pic);
        if (Verify == null) {
            return Result.error("图形验证码错误");
        }
        operations.getOperations().delete(CaptCha_Pic);
        User u = userService.findByUserEmail(Email);
        if (u != null && Type.equals("0")) return Result.error("该邮箱已被注册");
        StringBuffer code = new StringBuffer();
        Random random = new Random();
        String works = "1234567890ABCDEFGHIJKLMNOPQRSTUVWSYZ";
        for (int i = 0; i < 6; i++) {
            code.append(works.charAt(random.nextInt(works.length() - 1)));
        }
//        Boolean res = sendMailUtil.SendMail(AcceptMail,"云梦盘注册验证码","您的验证码为:" + code + ",请勿泄露给他人");
        if (Type.equals("0")) {
            Type = "注册";
        } else if (Type.equals("3")) {
            Type = "重置";
        }
        //以邮箱为标识生成验证码
        operations.set(Email, code.toString().toUpperCase(), 2, TimeUnit.MINUTES);
        Boolean res = sendMailUtil.RegistryCaptChaMail(Email, "云梦盘", String.valueOf(code), Type, "2分钟");
        System.out.println("验证码：" + code);
        if (res) return Result.success("验证码发送成功");
        return Result.error("验证码发送失败");
    }

    //注册
    @PostMapping("/register")
    public Result register(String Email, String Password, String CaptCha) {
        if (Email.equals("") || Password.equals("") || CaptCha.equals("")) return Result.error("参数为空");

        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String res = operations.get(Email);
        if (res == null) return Result.error("验证码不存在");
        if (!res.equals(CaptCha.toUpperCase())) return Result.error("验证码错误");
        operations.getOperations().delete(Email);
        //查询是否存在用户
        User u = userService.findByUserEmail(Email);
        if (u == null) {
            //注册
            String CardId = userService.register(Email, Password);

            String AvatarUrl = "D:/Cloud_Dream_Disk/user_pic/" + CardId;
            String StorageUrl = "D:/Cloud_Dream_Disk/storage/" + CardId;
            String RecycleBinUrl = "D:/Cloud_Dream_Disk/recycleBin/" + CardId;
            String ChunkingUrl = "D:/Cloud_Dream_Disk/chunking/" + CardId;
            FileOperationsUtil.CreateDirectory(AvatarUrl);
            FileOperationsUtil.CreateDirectory(StorageUrl);
            FileOperationsUtil.CreateDirectory(RecycleBinUrl);
            FileOperationsUtil.CreateDirectory(ChunkingUrl);

//            return Result.success(CardId);
            return Result.success("注册成功");
        } else {
            return Result.error("该邮箱已被注册");
        }
    }

    //登录
    @PostMapping("/login")
    public Result login(String Email, String Password, String CaptCha, HttpServletRequest request) {
        System.out.println(CaptCha);
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String res = operations.get(CaptCha.toUpperCase());
        if (res == null) return Result.error("图形验证码已过期");
        operations.getOperations().delete(CaptCha);
        User u = userService.findByUserEmail(Email);
        if (u == null) return Result.error("账号不存在");

        Password = Md5Util.getMD5String(Password + "Dream");
        if (!u.getPassword().equals(Password)) return Result.error("密码错误");
        String ip = GetIpAddressUtil.getIpAddr(request);
        String device = GetDeviceInfoUtil.getDeviceInfo(request);
        if (device.equals("非法请求,数据不存在")) return Result.error("数据异常");

        List<LoginInfo> loginInfo = loginInfoService.findLoginInfo(u.getUserCardID());

        if (loginInfo.size() != 0) {
            Boolean result = loginInfoService.IpSelect(ip,u.getUserCardID());
            if (!result) return Result.error("该IP已被禁止登录");
            for (int i = 0; i < loginInfo.size(); i++) {
                if (loginInfo.get(i).getIpAddr().equals(ip) && loginInfo.get(i).getDevice().equals(device)) {
                    userService.updateLoginInfo(u.getUserCardID(), ip, device);
                    break;
                } else if (i == loginInfo.size() - 1) {
//                System.out.println(u.getUserCardID() + " " + ip + " " + device);
                    userService.AddLoginInfo(u.getUserCardID(), ip, device);
                }
            }
        }else{
            userService.AddLoginInfo(u.getUserCardID(), ip, device);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getUserCardID());
        map.put("username", u.getUsername());
        map.put("status", (int) u.getStatus());
        map.put("permissions", (int) u.getPermissions());
        String Token = JWTUtil.GetToken(map);
        operations.set(Token, Token, 6, TimeUnit.HOURS);

        return Result.success(Token);
    }
    //我的用户信息
    @GetMapping("/MyUserInfo")
    public Result<User> MyUserInfo() {
        Map<String, Object> map = ThreadLocalUtil.get();
        String id = (String) map.get("id");
        User u = userService.findMyInfo(id);
        String NewEmail = u.getEmail().substring(0,3) + "***" + u.getEmail().substring(u.getEmail().lastIndexOf("@"));
        u.setEmail(NewEmail);
        if (u.getPhone() != null){
            String NewPhone = u.getPhone().substring(0,3) + "***" + u.getPhone().substring(u.getPhone().length()-4);
            u.setPhone(NewPhone);
        }
        if (u.getUserPic() != null) {
            u.setUserPic(u.getUserPic().substring(u.getUserPic().lastIndexOf("/") + 1));
        }
        return Result.success(u);
    }

    //重置密码验证
    @PostMapping("/CheckECaptCha")
    public Result CheckECaptCha(String ECaptCha,String Email) throws JsonProcessingException {
        if (!(ECaptCha.length() >= 6)) return Result.error("错误验证码");
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String res = operations.get(Email);
        if (res == null) return Result.error("验证码不存在");
        System.out.println(res + "  " + ECaptCha.toUpperCase());
        if (!res.equals(ECaptCha.toUpperCase())) return Result.error("验证码错误");
        operations.getOperations().delete(Email);
        //生成一个5分钟内有效的修改验证码
        long timeStamp = System.currentTimeMillis();
        String ValidateToken = Md5Util.getMD5String(timeStamp + "By Dream_沫202432");
        operations.set(ValidateToken, Email, 5, TimeUnit.MINUTES);

//        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        map.put("ValidateToken", ValidateToken);
        map.put("timeStamp", timeStamp);

//        return Result.success(objectMapper.writeValueAsString(map));
        return Result.success(map);
    }

    //重置密码
    @PatchMapping("/RestPassword")
    public Result RestPassword(String ValidateToken, String timeStamp, String NewPassword, String RePassword) {
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String res = operations.get(ValidateToken);
        if (res == null) return Result.error("参数异常");
        String Validate = Md5Util.getMD5String(timeStamp + "By Dream_沫202432");
        if (!ValidateToken.equals(Validate)) return Result.error("token参数异常");
        if (!NewPassword.equals(RePassword)) return Result.error("两次密码不一致");
        NewPassword = Md5Util.getMD5String(NewPassword + "Dream");
        userService.RestPassword(NewPassword,res);
        operations.getOperations().delete(ValidateToken);
        return Result.success("修改成功");
    }

    //修改个人信息
    @PatchMapping("/UpdateInfo")
    public Result UpdateInfo(String username,String phone,String dateOfBirth){//User user) {
//        if (user.getUsername().equals("")) return Result.error("参数不能为空");
        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        if (dateOfBirth != null){
            user.setDateOfBirth(LocalDateTime.parse(dateOfBirth));
        }
        userService.UpdateInfo(user);
        return Result.success();
    }
    //用户状态以及token检验
    @GetMapping("/detect")
    public Result detect(){
        Map<String,String> map = ThreadLocalUtil.get();
//        System.out.println(map);
        if (map == null) return Result.error("请重新登录");
        String UserId = map.get("id");
        User u = userService.findMyInfo(UserId);
        if (u == null || u.getStatus() == 0) return Result.error(u == null? "账号不存在":"账号已被冻结");
        return Result.success();
    }
}
