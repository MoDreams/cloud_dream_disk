package com.cloud_disk.cloud_dream_disk.controller;

import com.cloud_disk.cloud_dream_disk.mapper.UserMapper;
import com.cloud_disk.cloud_dream_disk.pojo.FilesInfo;
import com.cloud_disk.cloud_dream_disk.pojo.Result;
import com.cloud_disk.cloud_dream_disk.service.FileUpLoadService;
import com.cloud_disk.cloud_dream_disk.service.MyFilesService;
import com.cloud_disk.cloud_dream_disk.utils.FileOperationsUtil;
import com.cloud_disk.cloud_dream_disk.utils.SizeConversionUtil;
import com.cloud_disk.cloud_dream_disk.utils.ThreadLocalUtil;
import com.cloud_disk.cloud_dream_disk.utils.UpLoadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/MyFiles")
public class MyFilesController {
    //头像路径
    @Value("${storage.AvatarRootUrl}")
    private String AvatarRootUrl;
    //文件存储路径
    @Value("${storage.FileRootUrl}")
    private String FileRootUrl;
    //回收站路径
    @Value("${storage.recycleBinRootUrl}")
    private String recycleBinRootUrl;
    @Autowired
    private MyFilesService myFilesService;
    @Autowired
    private UserMapper userMapper;
    //查看文件夹中的文件
    @GetMapping("/list")
    public Result<List> list(String url, Integer del) {
        Map<String, Object> thread = ThreadLocalUtil.get();
        String userId = (String) thread.get("id");
        File file = new File(FileRootUrl + userId + url);
        if (!file.exists()) return Result.error("文件夹不存在");
        try {
            if (del == 0) {
                List<Map<String, Object>> list = FileOperationsUtil.ViewDirectory(FileRootUrl + userId + url, del);
//                System.out.println("Y");
                List NewList = myFilesService.selectState(list, del);
                return Result.success(NewList);
            } else {
                List<Map<String, Object>> list = myFilesService.SelectReclaim(userId);
                return Result.success(list);
            }
        } catch (Exception e) {
            System.out.println("Con查看列表异常:" + e.getMessage());
            return Result.error("出现未知错误,204434,请联系管理人员");
        }
    }

    //新建文件夹
    @PostMapping("/createFolder")
    public Result create(String url, String name) {
        String[] SpecialCharacters = {"/", "\\", ":", "：", "*", "\"", "<", ">", "|"};
        Map<String, String> map = ThreadLocalUtil.get();
        String UserId = map.get("id");
        for (int i = 0; i < SpecialCharacters.length; i++) {
            if (name.lastIndexOf(SpecialCharacters[i]) != -1) return Result.error("创建失败,存在非法字符");
        }
        String path = FileRootUrl + UserId + url + name;
        File file = new File(path);
        if (file.exists()) return Result.error("文件夹已存在");
        try {
            FileOperationsUtil.CreateDirectory(path);
            FilesInfo filesInfo = new FilesInfo();
            filesInfo.setUserId(UserId);
            filesInfo.setFileName(name);
            filesInfo.setFilePath(path);
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            filesInfo.setCreateTime(LocalDateTime.parse(currentTime.format(dateTimeFormat), dateTimeFormat));
            filesInfo.setLastUpdateTime(LocalDateTime.parse(currentTime.format(dateTimeFormat), dateTimeFormat));
            filesInfo.setFolderType(1);
            filesInfo.setFileCategory(0);
            filesInfo.setDelFlag(0);
            myFilesService.add(filesInfo);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Result.error("出现未知错误,205444,请联系管理人员");
        }
        return Result.success("创建成功");
    }

    //删除文件夹
    @PostMapping("/deleteFolder")
    public Result deleteFolder(@RequestBody ArrayList<String> list) {
        Map<String, String> map = ThreadLocalUtil.get();
        String UserId = map.get("id");
        String Del_Str = "BY沫梦[回收站&标识符]1710836750184";
        try {
            for (int i = 0; i < list.size(); i++) {
                String urlName = list.get(i) + Del_Str;
                System.out.println(FileRootUrl + UserId + urlName);
                File file = new File(FileRootUrl + UserId + urlName);
                System.out.println(file.exists());
                if (file.listFiles() == null) {
                    //删除空文件夹
                    if (!file.exists()) return Result.error("'" + list.get(i).replace("/", "") + "' 不存在");
                    FileOperationsUtil.DeleteDirectory(FileRootUrl + UserId + urlName);
                    List<String> list1 = new ArrayList<>();
                    list1.add(FileRootUrl + UserId + urlName);
                    myFilesService.deleteFile(list1, UserId);
                } else {
                    //递归删除子文件夹
                    List<String> fileUrl = FileOperationsUtil.DeleteFolderRecursively(file);
                    fileUrl.add(FileRootUrl + UserId + urlName);
                    myFilesService.deleteFile(fileUrl, UserId);
                }
            }
            Long res = FileOperationsUtil.UseSpace(FileRootUrl + UserId);
            userMapper.updateUseSize(UserId,res);
            return Result.success("删除成功");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Result.error("出现未知错误,206454,请联系管理人员");
        }
    }

    //修改文件/文件夹名称
    @PostMapping("/ReviseFolderName")
    public Result ReviseFolderName(String url, String ReviseName) {
        String[] SpecialCharacters = {"/", "\\", ":", "：", "*", "\"", "<", ">", "|"};
        for (int i = 0; i < SpecialCharacters.length; i++) {
            if (ReviseName.lastIndexOf(SpecialCharacters[i]) != -1) return Result.error("修改失败,存在非法字符");
        }
        Map<String, Object> map = ThreadLocalUtil.get();
        String userId = (String) map.get("id");
        String[] splitUrl = url.split("/");
        String NewUrl = FileRootUrl + userId;
        for (int i = 0; i < splitUrl.length; i++) {
            if (i == splitUrl.length - 1) {
                NewUrl += ReviseName;
            } else {
                NewUrl += splitUrl[i] + "/";
            }
            ;
        }
        File file = new File(NewUrl);
        System.out.println(NewUrl);
        if (file.exists()) return Result.error("文件名已重复");
        Boolean res = FileOperationsUtil.ReviseFolderName(FileRootUrl + userId + url, NewUrl);
        myFilesService.moveUpdate(userId, FileRootUrl + userId + url, NewUrl);
        if (res) return Result.success("修改成功");
        return Result.error("修改失败");
    }

    //内存计算
    @GetMapping("/useSpace")
    public Result UseSpace() {
        Map<String, String> map = ThreadLocalUtil.get();
        String UserId = map.get("id");
        Double res = Double.valueOf(FileOperationsUtil.UseSpace(FileRootUrl + UserId));
        String Space = SizeConversionUtil.ByteAutomaticConversion(res);
        return Result.success(Space);
    }

    //移动文件
    @PostMapping("/MoveFile")
    public Result MoveFile(String Url, String NewUrl) throws IOException {
        Map<String, String> map = ThreadLocalUtil.get();
        String UserId = map.get("id");
        NewUrl = FileRootUrl + UserId + NewUrl;
        File file = new File(NewUrl);
        if (file.exists()) return Result.error("文件名重复");
        FileOperationsUtil.CreateDirectory(NewUrl);
        Boolean res = null;
        try {
            res = FileOperationsUtil.MoveFile(FileRootUrl + UserId + Url, NewUrl);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return Result.error("文件名重复");
        }
        if (res) {
            myFilesService.moveUpdate(UserId, FileRootUrl + UserId + Url, NewUrl);
            return Result.success("移动成功！");
        }
        return Result.error("失败");
    }

    //回收站/移出回收站
    @PostMapping("/reclaim")
    public Result Reclaim(@RequestBody ArrayList<Map<String, String>> list) {
        Map<String, String> map = ThreadLocalUtil.get();
        String UserId = map.get("id");
        String Del_Str = "BY沫梦[回收站&标识符]1710836750184";
        int res = 0;
        Integer del = Integer.valueOf(list.get(0).get("del"));
        for (int i = 0; i < list.size(); i++) {
            String newUrl = list.get(i).get("url") + Del_Str;
            String reName = list.get(i).get("url");
            if (del == 1) {
                myFilesService.reclaim(FileRootUrl + UserId + reName, UserId, 1);
                FileOperationsUtil.ReviseFolderName(FileRootUrl + UserId + reName, FileRootUrl + UserId + newUrl);
                myFilesService.moveUpdate(UserId, FileRootUrl + UserId + reName, FileRootUrl + UserId + newUrl);
                res = 0;
            } else if (del == 2) {
                File file = new File(FileRootUrl + UserId + reName);
                if (file.exists()) reName += " 恢复";
                myFilesService.reclaim(FileRootUrl + UserId + newUrl, UserId, 0);
                FileOperationsUtil.ReviseFolderName(FileRootUrl + UserId + newUrl, FileRootUrl + UserId + reName);
                myFilesService.moveUpdate(UserId, FileRootUrl + UserId + newUrl, FileRootUrl + UserId + reName);
                res = 1;
            }
        }
        if (res == 0) {
            return Result.success("已进入回收站,30天后将自动删除");
        } else if (res == 1) {
            return Result.success("已移出回收站");
        }
        return Result.error("失败,未知错误203254,请联系管理人员");
    }

    //获取文件数
    @GetMapping("/GetFilesNum")
    public Result GetFilesNum(Integer type) {
        List<Integer> list = myFilesService.GetFilesNum(type);
        if (list != null) return Result.success(list);
        return Result.error("出现未知异常");
    }

    //获取一周上传文件数
    @GetMapping("/GetWeekFilesNum")
    public Result GetWeekFilesNum(Integer type) {
        List<List<Integer>> list = myFilesService.GetWeekFilesNum();
        if (list != null) return Result.success(list);
        return Result.error("出现未知异常");
    }

    //查找文件
    @GetMapping("/FindFile")
    public Result FindFile(String name,Integer type){
        List<Map<String,Object>> list = myFilesService.FindFile(name,type);
        return Result.success(list);
    }
}
