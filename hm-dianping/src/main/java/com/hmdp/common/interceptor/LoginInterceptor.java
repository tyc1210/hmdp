package com.hmdp.common.interceptor;

import com.hmdp.common.exception.BaseException;
import com.hmdp.utils.JwtHelper;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 类描述
 *
 * @author tyc
 * @version 1.0
 * @date 2022-10-09 11:46:04
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token
        String token = request.getHeader("authorization");
        // 校验token
        Long userId = JwtHelper.getUserId(token);
        if(null == userId){
            throw new BaseException("token 无效");
        }
        return true;
    }

}
