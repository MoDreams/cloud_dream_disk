package com.cloud_disk.cloud_dream_disk.mapper;

import com.cloud_disk.cloud_dream_disk.pojo.ShardFile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShareFileMapper {
    @Insert("insert into shard(UserId,Md5Path,FilePath,CreateTime,LastTime)" +
            "values(#{UserId},#{Md5Path},#{FilePath},#{CreateTime},#{LastTime})")
    void createShare(ShardFile shardFile);

    @Select("select * from shard where Md5Path=#{md5Path}")
    ShardFile list(String md5Path);
}
