package com.cloud_disk.cloud_dream_disk.mapper;

import com.cloud_disk.cloud_dream_disk.pojo.FilesInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MyFileMapper {
    @Delete("delete from file_info where file_path=#{fileUrl} and user_id=#{userId}")
    void deleteFile(String fileUrl, String userId);

    @Update("update file_info set file_path=#{moveUrl},file_name=#{FileName} where user_id=#{userId} and file_path=#{url}")
    void moveUpdate(String userId, String url, String moveUrl, String FileName);

    @Select("select * from file_info where file_path=#{url} and del_flag=#{del} and user_id=#{userId}")
    FilesInfo selectState(String url, Integer del, String userId);

    @Select("select * from file_info where del_flag=1 and user_id=#{userId}")
    List<FilesInfo> SelectReclaim(String userId);

    @Update("update file_info set del_flag=#{del_flag},recovery_time=#{recovery_time} where user_id=#{userId} and file_path=#{url}")
    void reclaim(String url, String userId, Integer del_flag, LocalDateTime recovery_time);

    @Select("select count(*) from file_info where user_id=#{userId} and file_category=#{i}")
    Integer GetFilesNum(String userId, int i);

    @Select("select count(*) from file_info where create_time >= #{New} and create_time <= #{End} and user_id=#{userId} and file_category=#{type}")
    Integer GetWeekFilesNum(String userId, String New, String End, Integer type);

    @Select("select * from file_info where user_id=#{userId} and del_flag=0 and file_name like CONCAT('%',#{name},'%')")
    List<FilesInfo> FindFile(String name, String userId);
    @Select("Select * from file_info where user_id=#{userId} and del_flag=0 and file_category=#{type}")
    List<FilesInfo> FindCategory(String userId, Integer type);
}
