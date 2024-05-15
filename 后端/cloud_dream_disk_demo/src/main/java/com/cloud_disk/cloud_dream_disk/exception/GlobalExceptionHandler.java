package com.cloud_disk.cloud_dream_disk.exception;

import com.cloud_disk.cloud_dream_disk.pojo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result handlerException(Exception e){
        e.printStackTrace();
        return Result.error("数据异常,请联系管理人员");
    }
}
