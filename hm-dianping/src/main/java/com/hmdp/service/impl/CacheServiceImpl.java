package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Shop;
import com.hmdp.entity.User;
import com.hmdp.service.ICacheService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.common.RedisConstant.*;

/**
 * 类描述
 *
 * @author tyc
 * @version 1.0
 * @date 2022-10-09 10:19:27
 */
@Service
public class CacheServiceImpl implements ICacheService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveCode(String phone, String code) {
        stringRedisTemplate.opsForValue().set(String.format(PHONE_CODE,phone),code,5, TimeUnit.MINUTES);
    }

    @Override
    public String getCode(String phone) {
        return stringRedisTemplate.opsForValue().get(String.format(PHONE_CODE,phone));
    }

    @Override
    public void saveUser(User user) {
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        String key = String.format(USER,user.getId());
        stringRedisTemplate.opsForHash().putAll(key,userMap);
    }

    @Override
    public Shop getShopById(Long id) {
        String strShop = stringRedisTemplate.opsForValue().get(String.format(CACHE_SHOP,id));
        if(StringUtils.isEmpty(strShop)){
            return null;
        }
        return JSONUtil.toBean(strShop, Shop.class);
    }

    @Override
    public void saveShop(Shop shop) {
        stringRedisTemplate.opsForValue().set(String.format(CACHE_SHOP,shop.getId()),JSONUtil.toJsonStr(shop),30,TimeUnit.MINUTES);
    }
}
