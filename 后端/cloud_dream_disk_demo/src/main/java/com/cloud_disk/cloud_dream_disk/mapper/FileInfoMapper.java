package com.cloud_disk.cloud_dream_disk.mapper;

import com.cloud_disk.cloud_dream_disk.pojo.FilesInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FileInfoMapper {
    @Insert("insert into file_info(user_id,file_md5,file_name,file_path,create_time,last_update_time,folder_type,file_category,del_flag) " +
            "values(#{userId},#{fileMd5},#{fileName},#{filePath},now(),now(),#{folderType},#{fileCategory},#{delFlag})")
    void add(FilesInfo filesInfo);
    @Select("select * from file_info where file_path=#{path}")
    FilesInfo SelectFile(String path);
    @Select("select * from file_info where file_md5=#{md5}")
    FilesInfo SelectFileMd5(String md5);
    @Select("select * from file_info where file_md5=#{md5} and user_id=#{userId} and file_path=#{Path}")
    FilesInfo SelectMyFile(String md5, String userId,String Path);
}
