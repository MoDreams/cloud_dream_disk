package com.cloud_disk.cloud_dream_disk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan(basePackages = "mappers")
public class CloudDreamDiskApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudDreamDiskApplication.class, args);
    }
}
