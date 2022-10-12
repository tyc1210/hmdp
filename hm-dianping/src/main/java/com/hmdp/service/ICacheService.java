package com.hmdp.service;

import com.hmdp.entity.Shop;
import com.hmdp.entity.User;

/**
 * 类描述
 *
 * @author tyc
 * @version 1.0
 * @date 2022-10-09 10:17:50
 */
public interface ICacheService {
    /**
     * 记录发送的验证码
     * @param phone
     * @param code
     */
    void saveCode(String phone, String code);

    String getCode(String phone);

    void saveUser(User user);

    Shop getShopById(Long id);

    void saveShop(Shop shop);
}
