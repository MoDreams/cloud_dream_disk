package com.cloud_disk.cloud_dream_disk.controller;

import com.cloud_disk.cloud_dream_disk.Config.NonStaticResourceHttpRequestHandler;
import com.cloud_disk.cloud_dream_disk.mapper.UserMapper;
import com.cloud_disk.cloud_dream_disk.pojo.*;
import com.cloud_disk.cloud_dream_disk.service.FileUpLoadService;
import com.cloud_disk.cloud_dream_disk.service.MyFilesService;
import com.cloud_disk.cloud_dream_disk.service.ShareFileService;
import com.cloud_disk.cloud_dream_disk.service.UserService;
import com.cloud_disk.cloud_dream_disk.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class FileUpLoadController {
    @Autowired
    private UserService userService;
    @Autowired
    private FileUpLoadService fileUpLoadService;
    @Autowired
    private ShareFileService shareFileService;
    @Value("${storage.FileRootUrl}")
    private String FileRootUrl;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NonStaticResourceHttpRequestHandler nonStaticResourceHttpRequestHandler;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //上传图片
    @PostMapping("/uploadPic")
    public Result uploadPic(MultipartFile multipartFile) throws IOException {
        System.out.println("yl");
        Map<String, Object> map = ThreadLocalUtil.get();
        String UserId = (String) map.get("id");
        String PicName = multipartFile.getOriginalFilename();
        String suffix = PicName.substring(PicName.lastIndexOf("."));
        String NewPicName = UUID.randomUUID() + suffix;
        String url = "D:/Cloud_Dream_Disk/user_pic/" + UserId;
        Boolean res = UpLoadUtil.UpLoad(url, NewPicName, multipartFile.getInputStream());
        String NewPicUrl = url + "/" + NewPicName;
        userService.updateAvatar(NewPicUrl);
        if (res) return Result.success(NewPicName);
        return Result.error("图片上传失败");
    }

    //查看头像图片
    @GetMapping("/loadAvatarPic")
    public Result loadAvatarPic(String PicUrl) throws IOException {
        System.out.println(PicUrl);
        Map<String, Object> map = ThreadLocalUtil.get();
        String UserId = (String) map.get("id");

        //第一次使用没设置图片
        if (PicUrl.equals("/null")) {
            PicUrl = "D:/Cloud_Dream_Disk/user_pic/Pic.jpeg";
        } else {
            String url = "D:/Cloud_Dream_Disk/user_pic/" + UserId;
            PicUrl = url + "/" + PicUrl;
        }
        String Base64Img = ReadImage.ImageToBase64(PicUrl);
        if (Base64Img != null) return Result.success(Base64Img);
        return Result.error("转换失败");
    }

    //查看图片
    @GetMapping("/loadPic")
    public Result loadPic(String url, Integer type) throws FileNotFoundException {
        Map<String, String> map = ThreadLocalUtil.get();
        String UserId = map.get("id");
        url = FileRootUrl + UserId + url;
        String Base64Img = fileUpLoadService.loadPic(url, type);
        if (Base64Img != null) return Result.success(Base64Img);
        return Result.error("图片查看失败");
    }

    @GetMapping("/chunk")
    //检查分片是否存在
    public Result checkChunkExist(FileChunk fileChunk) throws IOException {
        //自写
        Map<String, String> map = ThreadLocalUtil.get();
        String UserId = map.get("id");
        User u = userMapper.findMyInfo(UserId);
        Long useSpace = u.getUseSpace() + fileChunk.getTotalSize();
        if (useSpace > u.getTotalSpace()) {//上传大小小于剩余容量就不进行上传
            return Result.error("内存已满");
        }
        //查询数据库中是否存在该文件
        Boolean exits = fileUpLoadService.SelectFile(fileChunk.getIdentifier(), fileChunk.getUrl(), fileChunk.getFilename());
        if (exits) {
            FileChunkResult fileChunkResult = new FileChunkResult();
            fileChunkResult.setSkipUpload(true);
            return Result.success(fileChunkResult);//("秒传完毕");
        }
        //不存在，检查是否有已经上传的分片
        FileChunkResult fileChunkResult = fileUpLoadService.checkChunkExist(fileChunk);

        return Result.success(fileChunkResult);

    }

    //上传分片
    @PostMapping("/chunk")
    public Result uploadChunk(FileChunk fileChunk) throws IOException {
        try {

            fileUpLoadService.uploadChunk(fileChunk);
        } catch (IOException e) {
            System.out.println("分片上传异常: " + e.getMessage());
            return Result.error("分片上传异常");
        }
        return Result.success("分片上传成功");
    }

    //合并分片
    @PostMapping("/merge")
    public Result mergeChunk(@RequestBody FileChunk fileChunk) {
        try {
            List<Integer> res = fileUpLoadService.mergeChunks(fileChunk);
            if (res == null) return Result.success("上传成功");
            return Result.error("分片不完整或不存在", res);
        } catch (Exception e) {
            System.out.println("Con分片合并出现异常：" + e.getMessage());
            return Result.error("分片合并出现异常");
        }
    }

    //获取分片下载文件参数
    @PostMapping("/ShardDetails")
    public Result ShardDetails(@RequestBody ArrayList<Map<String, String>> lists) {//String md5, String Path) {
        List<Map<String, Object>> list = fileUpLoadService.ShardDetails(lists);
        return Result.success(list);
    }

    //下载
    @PostMapping("/Download")
    public Result Download(Integer ShardNum, String md5, String Path) throws IOException {
        Map<String, Object> map = fileUpLoadService.Download(ShardNum, md5, Path);
        if (map != null) return Result.success(map);
        return Result.error("该分片读取出现异常");
    }

    //浏览器下载
    @GetMapping("/BrowserDownload")
    public ResponseEntity<Resource> BrowserDownload(String path) throws UnsupportedEncodingException {
        Map<String, String> map = ThreadLocalUtil.get();
        String UserId = map.get("id");
        path = FileRootUrl + UserId + path;
        File file = new File(path);
        return fileUpLoadService.RespDownload(file);
    }

    //分享下载
    @GetMapping("/shardDownload")
    public ResponseEntity<Resource> shardDownload(String path) throws UnsupportedEncodingException {

        ShardFile shardFile = shareFileService.list(path);
        if (LocalDateTime.now().isBefore(shardFile.getLastTime())) {//如果当前时间小于过期时间
            File file = new File(shardFile.getFilePath());
            return fileUpLoadService.RespDownload(file);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null);
    }
    //播放token
    @GetMapping("/PlayToken")
    public Result PlayToken(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String md5Token = Md5Util.getMD5String(Md5Util.getMD5String(token) + Md5Util.getMD5String(String.valueOf(System.currentTimeMillis())) + "沫梦_2024");
        operations.set(md5Token,token,1, TimeUnit.MINUTES);
        return Result.success(md5Token);
    }

    //获取视频、音频数据在线播放
    @GetMapping("/Video")
    public void video(HttpServletRequest request, HttpServletResponse response, String url, String token) throws UnsupportedEncodingException {
        String UserId = UserId(token);
        String path = FileRootUrl + UserId + url;
        try {
            File file = new File(path);
            if (file.exists()) {
                request.setAttribute(NonStaticResourceHttpRequestHandler.ATTR_FILE, path);
                nonStaticResourceHttpRequestHandler.handleRequest(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    //预览文档
    @GetMapping("/doc")
    public ResponseEntity<Resource> doc(String url,String token) throws UnsupportedEncodingException {
        String UserId = UserId(token);
        String path = FileRootUrl + UserId + url;
        File file = new File(path);
        if (file.exists()){
            return fileUpLoadService.RespDownload(file);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null);
    }
    //token中获取UserId
    public String UserId(String token){
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String res = operations.get(token);
        if (res == null) throw new RuntimeException();
        Map<String, Object> map = JWTUtil.ParamsToken(res);
        String UserId = (String) map.get("id");
        return UserId;
    }

}
