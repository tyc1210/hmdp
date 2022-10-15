package com.hmdp.common;

/**
 * 类描述
 *
 * @author tyc
 * @version 1.0
 * @date 2022-10-09 13:37:48
 */
public class RedisConstant {
    /**
     * 短信验证码 key 值
     */
    public static String PHONE_CODE = "phone:%s";
    /**
     * 用户信息
     */
    public static String USER = "user:%s";
    /**
     * 商铺信息
     */
    public static String CACHE_SHOP = "shop:";

    /**
     * 商铺锁
     */
    public static String CACHE_SHOP_LOCK = "shop:lock:";
}
