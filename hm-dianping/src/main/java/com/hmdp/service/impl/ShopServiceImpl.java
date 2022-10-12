package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.hmdp.common.exception.BaseException;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.ICacheService;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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

    @Override
    public Shop getById(Long id){
        // 先从缓存获取
        Shop cacheShop = cacheService.getShopById(id);
        if(null != cacheShop){
            return cacheShop;
        }
        // 不存在则从数据库获取并放入缓存
        Shop shop = getById(id);
        if(null == shop){
            throw new BaseException("商铺不存在");
        }
        cacheService.saveShop(shop);
        return shop;
    }

    @Override
    public boolean updateById(Shop shop) {
        return false;
    }
}
