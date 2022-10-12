package com.hmdp.common.exception;

import com.hmdp.dto.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 类描述
 *
 * @author tyc
 * @version 1.0
 * @date 2022-10-09 09:44:27
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e){
        if(e instanceof BaseException){
            return Result.fail(e.getMessage());
        }else if(e instanceof MethodArgumentNotValidException){
            return Result.fail(((MethodArgumentNotValidException)e).getBindingResult().getFieldError().getDefaultMessage());
        }else {
            System.out.println(e.getClass());
            return Result.fail("system error");
        }
    }
}
