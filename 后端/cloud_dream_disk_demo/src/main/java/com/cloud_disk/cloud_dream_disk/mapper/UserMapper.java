package com.cloud_disk.cloud_dream_disk.mapper;

import com.cloud_disk.cloud_dream_disk.pojo.LoginInfo;
import com.cloud_disk.cloud_dream_disk.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from User where E_mail = #{Email}")
    User findByUserEmail(String Email);

    @Insert("insert into user(UserCardID,Password,E_mail,Create_Time)" +
            "values(#{CardId},#{password},#{Email},now())")
    void register(String Email, String password, String CardId);

    @Insert("insert into logininfo(UserId,IpAddr,Device,LoginTime)" +
            "values(#{userCardID},#{ip},#{device},now())")
    void AddLoginInfo(String userCardID, String ip, String device);

    @Update("update user set Password = #{md5String} where E_mail=#{Email}")
    void RestPassword(String md5String, String Email);

    @Select("select * from user where UserCardID = #{id}")
    User findMyInfo(String id);

    @Update("update logininfo set LoginTime = now() where UserId=#{userCardID} and IpAddr = #{ip} and Device = #{device}")
    void updateLoginInfo(String userCardID, String ip, String device);

    void UpdateInfo(User user);

    @Update("update user set User_Pic = #{newPicUrl} where UserCardID = #{userId}")
    void updateAvatar(String newPicUrl, String userId);

    @Update("update user set Use_Space=#{fileSize} where UserCardID = #{userId}")
    void updateUseSize(String userId, Long fileSize);
}
