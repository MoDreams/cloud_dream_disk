package com.cloud_disk.cloud_dream_disk.service.impl;

import com.cloud_disk.cloud_dream_disk.mapper.FileInfoMapper;
import com.cloud_disk.cloud_dream_disk.mapper.UserMapper;
import com.cloud_disk.cloud_dream_disk.pojo.*;
import com.cloud_disk.cloud_dream_disk.service.FileUpLoadService;
import com.cloud_disk.cloud_dream_disk.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileUpLoadServiceImpl implements FileUpLoadService {
    //头像根路径
    @Value("${storage.AvatarRootUrl}")
    private String AvatarRootUrl;
    //文件存储根路径
    @Value("${storage.FileRootUrl}")
    private String FileRootUrl;
    //回收站根路径
    @Value("${storage.recycleBinRootUrl}")
    private String RecycleBinRootUrl;
    //分片存储根路径
    @Value("${storage.ChunkingRootUrl}")
    private String ChunkingRootUrl;
    @Value("${storage.ShardingSize}")
    private int ShardingSize;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private FileInfoMapper fileInfoMapper;
    @Autowired
    private UserMapper userMapper;

    //检查是否已完整上传
    @Override
    public FileChunkResult checkChunkExist(FileChunk fileChunk) {
        FileChunkResult fileChunkResult = new FileChunkResult();
        //分片路径
        String FileFolderPath = getFolderPath(fileChunk.getIdentifier());
        //检查本地是否存在该分片，并且所有分片已经上传完成
        File ShardFilePath = new File(FileFolderPath);
        if (ShardFilePath.exists() && ShardFilePath.isDirectory()) {
            List<Integer> list = new ArrayList<>();
            for (File file1 : ShardFilePath.listFiles()) {
                String[] name = file1.toString().split("\\\\");
                list.add(Integer.valueOf(name[name.length - 1]));
            }
            //开启续传
            fileChunkResult.setSkipUpload(true);
            fileChunkResult.setUploaded(list);
        } else {
            FileOperationsUtil.CreateDirectory(FileFolderPath);
            fileChunkResult.setSkipUpload(false);
        }
        return fileChunkResult;
    }

    //上传分片
    @Override
    public void uploadChunk(FileChunk fileChunk) {
        String FileFolderPath = getFolderPath(fileChunk.getIdentifier());
        File file = new File(FileFolderPath + "/" + fileChunk.getChunkNumber());
        if (file.exists()) file.delete();
        //写入分块文件
        InputStream inputStream;
        try {
            inputStream = fileChunk.getFile().getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(FileFolderPath + "/" + fileChunk.getChunkNumber());
            IOUtils.copy(inputStream, fileOutputStream);
            inputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /*
     * String fileMd5 分块md5
     * String fileName 文件名称
     * Integer totalChunks 分块数量
     * String url 文件实际存储位置
     * */
    //合并分片
    @Override
    public List<Integer> mergeChunks(FileChunk fileChunk) {
        String FolderPath = getFolderPath(fileChunk.getIdentifier());
        String FilePath = getFilePath(fileChunk.getFilename(), fileChunk.getUrl());
        //检查分片完整性和分片是否存在
        List<Integer> list = checkChunks(FolderPath, fileChunk.getTotalChunks(), fileChunk.getLastShardSize());
//        System.out.println(list);
        String fileName = fileChunk.getFilename();
        if (list.isEmpty()) {
            File mergeFile = new File(FilePath);
            if (mergeFile.exists()){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                String reName = "_" + LocalDateTime.now().format(formatter);//System.currentTimeMillis();
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex != -1) {
                    fileName = fileName.substring(0, dotIndex) + reName + fileName.substring(dotIndex);
                } else {
                    fileName += reName;
                }
                FilePath = getFilePath(fileName,fileChunk.getUrl());
//                System.out.println(FilePath);
                mergeFile = new File(FilePath);
            }
//            System.out.println(mergeFile);
//            System.out.println(FilePath);
            //合并分片
            try {
                //创建一个RandomAccessFile对象，用于对文件进行随机读写操作，"rw"表示以读写模式打开文件。
                RandomAccessFile randomAccessFileWriter = new RandomAccessFile(mergeFile, "rw");
                byte[] buffer = new byte[1024];
                for (int i = 1; i <= fileChunk.getTotalChunks(); i++) {
                    File chunk = new File(getFolderPath(fileChunk.getIdentifier()) + "/" + i);
                    RandomAccessFile randomAccessFileReader = new RandomAccessFile(chunk, "r");
                    int len;
                    while ((len = randomAccessFileReader.read(buffer)) != -1) {
                        randomAccessFileWriter.write(buffer, 0, len);
                    }
                    randomAccessFileReader.close();
                }
                randomAccessFileWriter.close();
            } catch (Exception e) {
                System.out.println("合并分片出现异常：" + e.getMessage());
                return list;
            }
        } else {
            System.out.println("Impl分片不完整");
            return list;
        }


        Map<String, String> map = ThreadLocalUtil.get();
        String userId = map.get("id");
        //删除分片数据
        String ChunkPath = ChunkingRootUrl + userId + "/" + fileChunk.getIdentifier();
        File file = new File(ChunkPath);
        if (file.exists()) FileOperationsUtil.DeleteFolderRecursively(file);
        //mysql中存储一个md5方便搜索
        FilesInfo filesInfo = new FilesInfo();
        filesInfo.setFileMd5(fileChunk.getIdentifier());
        filesInfo.setUserId(userId);
        filesInfo.setFileName(fileName);
        filesInfo.setFilePath(FilePath);
        filesInfo.setFolderType(0);
        filesInfo.setFileCategory(FileCategory(fileChunk.getFilename()));
        filesInfo.setDelFlag(0);
        fileInfoMapper.add(filesInfo);
        //修改内存大小
        UpdateSpace(userId, FilePath);
        return null;
    }

    public void UpdateSpace(String userId, String FilePath) {
        //修改内存大小
        User u = userMapper.findMyInfo(userId);
        Long useSpace = u.getUseSpace();
        Long fileSize = new File(FilePath).length();
//        System.out.println(FilePath);
//        System.out.println(fileSize);
        useSpace += fileSize;
//        System.out.println(useSpace);
        userMapper.updateUseSize(userId, useSpace);
    }

    @Override
    public Boolean SelectFile(String fileMd5, String url, String fileName) throws IOException {
        Map<String, String> map = ThreadLocalUtil.get();
        String userId = map.get("id");

        FilesInfo filesInfo = fileInfoMapper.SelectFileMd5(fileMd5);
        //文件存在进行复制和重命名
        if (filesInfo != null) {
            File file = new File(FileRootUrl + userId + url + fileName);
            String NewFile;
            if (file.exists()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                String reName = "_" + LocalDateTime.now().format(formatter);//System.currentTimeMillis();
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex != -1) {
                    // 如果文件名中包含点，将ReName加到点的前面
                    fileName = fileName.substring(0, dotIndex) + reName + fileName.substring(dotIndex);
                } else {
                    // 如果文件名中没有点，将ReName加到最后面
                    fileName += reName;
                }
            }
            NewFile = FileRootUrl + userId + url + fileName;
            String oldUrl = filesInfo.getFilePath();
            //复制
            Boolean res = FileOperationsUtil.CopyFile(oldUrl, NewFile);
            //添加到数据库
            filesInfo.setFileName(fileName);
            filesInfo.setFilePath(NewFile);
            filesInfo.setUserId(userId);
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            filesInfo.setCreateTime(LocalDateTime.parse(currentTime.format(dateTimeFormat), dateTimeFormat));
            filesInfo.setLastUpdateTime(LocalDateTime.parse(currentTime.format(dateTimeFormat), dateTimeFormat));
            filesInfo.setFolderType(0);
            filesInfo.setFileCategory(FileCategory(fileName));
            filesInfo.setDelFlag(0);
            fileInfoMapper.add(filesInfo);
            if (res){
                UpdateSpace(userId,NewFile);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> ShardDetails(ArrayList<Map<String, String>> lists) {
        Map<String, String> maps = ThreadLocalUtil.get();
        String userId = maps.get("id");
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            String md5 = lists.get(i).get("md5");
            String Path = lists.get(i).get("path");
            Path = FileRootUrl + userId + Path;
            Map<String, Object> map = new HashMap<>();
            FilesInfo filesInfo = fileInfoMapper.SelectMyFile(md5, userId, Path);
            File file = new File(filesInfo.getFilePath());
            //计算分片数
            int ShardNum = 1;
            if (file.length() != 0) {
                BigDecimal shardNum = BigDecimal.valueOf(file.length()).divide(BigDecimal.valueOf(ShardingSize), 0, RoundingMode.CEILING);
                ShardNum = shardNum.intValue();
            }

            map.put("shardNum", ShardNum);
            map.put("fileSize", SizeConversionUtil.ByteAutomaticConversion((double) file.length()));
            map.put("fileMd5", md5);
//            map.put("name",lists.get(i).get("path").split("/")[lists.get(i).get("path").split("/").length-1]);
            map.put("path", lists.get(i).get("path"));
            list.add(map);
        }
        return list;
    }

    @Override
    public Map<String, Object> Download(Integer shardNum, String md5, String path) throws IOException {
        Map<String, String> maps = ThreadLocalUtil.get();
        String userId = maps.get("id");
        path = FileRootUrl + userId + path;
//        System.out.println(md5 + " " + userId + " " + path);
        FilesInfo filesInfo = fileInfoMapper.SelectMyFile(md5, userId, path);
        if (filesInfo != null) {
            File file = new File(filesInfo.getFilePath());
            if (file.exists()) {
                long startByte;
                FileInputStream inputStream = new FileInputStream(file);
                try {
                    Map<String, Object> map = new HashMap<>();
                    startByte = (long) ShardingSize * (shardNum - 1);
                    long readShardSize = file.length() - startByte;
                    if (readShardSize >= ShardingSize) {
                        readShardSize = ShardingSize;
                    }
                    byte[] bytes = new byte[(int) readShardSize];
                    inputStream.skip(startByte);
//                    System.out.println(readShardSize);
                    int bytesRead = inputStream.read(bytes, 0, (int) readShardSize);
                    map.put("shardSize", bytesRead);
                    map.put("shardData", Base64.getEncoder().encodeToString(bytes));
                    return map;
                } catch (Exception e) {
                    System.out.println("分片读取异常:" + e.getMessage());
                    inputStream.close();
                    return null;
                }
            } else {
                System.out.println("文件不存在");
                return null;
            }
        }
        System.out.println("文件数据查询失败");
        return null;
    }

    @Override
    public ResponseEntity<Resource> RespDownload(File file) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + file.getName());
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=" + URLEncoder.encode(file.getName(), "UTF-8"))
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new FileSystemResource(file));

    }

    @Override
    public String loadPic(String url, Integer type) throws FileNotFoundException {
        String Base64Img = null;
        if (type == 0){//无压缩图片
            Base64Img = ReadImage.ImageToBase64(url);
        } else if (type == 1){//压缩的缩略图
            FileInputStream fileInputStream = new FileInputStream(url);
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Thumbnails.of(fileInputStream).scale(0.8)
                        .outputQuality(0.10f)//压缩质量 范围（0.00--1.00）
                        .toOutputStream(out);
                Base64Img = Base64.getEncoder().encodeToString(out.toByteArray());
            } catch (Exception e) {
                System.out.println("Impl查看图片出现异常错误：" + e.getMessage());
                return null;
            }
        } else if (type == 2){ //视频第一帧图片
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);
            try{
                grabber.start();
                //读取第一帧
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage firstImg = converter.convert(grabber.grabImage());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(firstImg,"jpg",byteArrayOutputStream);
                Base64Img = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
                grabber.stop();
            } catch (IOException e) {
                System.out.println("Impl视频失败" + e.getMessage());
                return null;
            }
        }
        return Base64Img;
    }


    private Integer FileCategory(String FileName) {
        if (FileName.lastIndexOf(".") == -1) return 5;
        //aa.txt.html
        String[] fileName = FileName.split("\\.");
        //获取文件后缀
        String suffix = fileName[fileName.length - 1].toLowerCase();
        if (suffix.equals("")) return 5;
        //视频
        List<String> videoFormat = List.of("mkv", "mp4", "webm", "mov", "ts", "trp", "flv", "wmv", "asf", "avi", "rmvb", "vob", "dat", "mpeg", "mpg", "org");
        //音频
        List<String> audioFormat = List.of("mp3", "wav", "wma", "aac", "m4a", "flac", "ape", "ogg", "aiff", "m4b", "ac3", "dts");
        //图片
        List<String> ImgFormat = List.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
        //文档
        List<String> documentFormat = List.of("txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf");
        if (videoFormat.contains(suffix)) return 1;
        if (audioFormat.contains(suffix)) return 2;
        if (ImgFormat.contains(suffix)) return 3;
        if (documentFormat.contains(suffix)) return 4;
        return 5;
    }

    //查看分片是否存在
    private List<Integer> checkChunks(String fileFolderPath, Integer totalChunks, Integer lastShardSize) {
        List<Integer> list = new ArrayList<>();
        try {
            for (int i = 1; i <= totalChunks; i++) {
                File file = new File(fileFolderPath + "/" + i);
                if (!file.exists()) {
                    list.add(i);
                    continue;
                }
                if (totalChunks == 1) { //只有一个分片的情况
                    if (file.length() != lastShardSize) {
                        list.add(i);
                        continue;
                    }
                }
                if (i != totalChunks) {//排除最后一个分片检查其他分片是否完整
                    if (file.length() != ShardingSize) {
                        list.add(i);
                    }
                } else {
                    if (file.length() != lastShardSize) {
                        list.add(i);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("分片检验异常: " + e.getMessage());
        }
        return list;
    }

    //存到redis中
    private synchronized long saveToRedis(FileChunk fileChunk) {
        Set<Integer> uploaded = (Set<Integer>) stringRedisTemplate.opsForHash().get(fileChunk.getIdentifier(), "uploaded");
        if (uploaded == null) {
            uploaded = new HashSet<>(Arrays.asList(fileChunk.getChunkNumber()));
            HashMap<String, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("uploaded", uploaded);
            objectObjectHashMap.put("totalChunks", fileChunk.getTotalChunks());
            objectObjectHashMap.put("totalSize", fileChunk.getTotalSize());
            objectObjectHashMap.put("path", fileChunk.getFilename());
            stringRedisTemplate.opsForHash().putAll(fileChunk.getIdentifier(), objectObjectHashMap);
        } else {
            uploaded.add(fileChunk.getChunkNumber());
            stringRedisTemplate.opsForHash().put(fileChunk.getIdentifier(), "uploaded", uploaded);
        }
        return uploaded.size();
    }


    //分片路径
    private String getFolderPath(String fileMd5) {
        Map<String, String> map = ThreadLocalUtil.get();
        String userId = map.get("id");
        String FileFolderPath = ChunkingRootUrl + userId + "/" + fileMd5;
        return FileFolderPath;
    }

    //文件实际存储路径
    private String getFilePath(String fileName, String url) {
        Map<String, String> map = ThreadLocalUtil.get();
        String userId = map.get("id");
        String FilePath = FileRootUrl + userId + url + fileName;
        return FilePath;
    }

}
