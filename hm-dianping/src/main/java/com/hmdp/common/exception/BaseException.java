package com.hmdp.common.exception;

import lombok.Data;

/**
 * 类描述
 *
 * @author tyc
 * @version 1.0
 * @date 2022-10-09 09:36:10
 */
@Data
public class BaseException extends RuntimeException{
    private String msg;
    private Integer code;

    public BaseException(String msg,Integer code){
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public BaseException(String msg) {
        super(msg);
        this.msg = msg;
    }
}
