package com.cloud_disk.cloud_dream_disk.mapper;

import com.cloud_disk.cloud_dream_disk.pojo.LoginInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
@Mapper
public interface LoginInfoMapper {
    @Select("select * from logininfo where UserId = #{userCardID}")
    List<LoginInfo> findLoginInfo(String userCardID);
    @Select("select * from logininfo where UserId = #{userId} and IpAddr = #{ip}")
    List<LoginInfo> findInfo(String ip, String userId);
    @Update("update logininfo set status=#{status} where UserId = #{userId} and IpAddr=#{ip} ")
    void UpdateStatus(String ip, Integer status, String userId);
}
