package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.hmdp.common.exception.BaseException;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.ICacheService;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static com.hmdp.common.RedisConstant.CACHE_SHOP;
import static com.hmdp.common.RedisConstant.CACHE_SHOP_LOCK;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    private ICacheService cacheService;

    @Resource
    private CacheClient cacheClient;

    @Override
    public Shop findById(Long id){
        Shop shop = cacheClient
                .queryWithLogicalExpire(CACHE_SHOP,id,Shop.class,id1->getById(id1),30L, TimeUnit.MINUTES,CACHE_SHOP_LOCK);
        return shop;
    }

    @Override
    public boolean updateById(Shop shop) {
        return false;
    }
}
