package com.hmdp.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.RedisExpireData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 *
 */
@Component
@Slf4j
public class CacheClient {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_BUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    private final String EMPTY_DATA = "";

    /**
     * 普通set
     */
    public void set(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }

    /**
     * 带有逻辑过期时间的set
     * 后续用于解决缓存击穿
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){
        RedisExpireData redisData = new RedisExpireData(LocalDateTime.now().plusSeconds(unit.toSeconds(time)), value);
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 解决缓存击穿,保证数据一致性,使用不当会造成死锁
     * 解决缓存穿透
     */
    public <R,ID>R queryWithPassThrough(String keyPre, ID id, Class<R> type,
                                       Function<ID,R> dbFallBack,Long time,TimeUnit unit,String lockKeyPre){
        String key = keyPre + id;
        R r = null;
        // 从redis查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 查询命中
        if(StrUtil.isNotBlank(json)){
            return JSONUtil.toBean(json,type);
        }
        // 防止缓存穿透将数据库中无的数据记为""放入缓存
        if("".equals(json)){
            return null;
        }
        // 获取锁开启缓存重建
        String lockKey = lockKeyPre + id;
        try {
            if(tryLock(lockKey)){
                // 从数据库查询
                r = dbFallBack.apply(id);
                if(null == r){
                    // 防止缓存穿透将""放入缓存
                    stringRedisTemplate.opsForValue().set(key,EMPTY_DATA);
                    return null;
                }
                // 放入缓存
                this.set(key,r,time,unit);
            }else {
                TimeUnit.MILLISECONDS.sleep(100);
                queryWithLogicalExpire(keyPre,id,type,dbFallBack,time,unit,lockKeyPre);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            unLock(lockKey);
        }
        return r;
    }

    /**
     * 解决缓存击穿,不保证数据一致性，加逻辑时间
     * 解决缓存穿透
     */
    public <R,ID>R queryWithLogicalExpire(String keyPre, ID id, Class<R> type,
                                         Function<ID,R> dbFallBack,Long time,TimeUnit unit,String lockKeyPre){
        String key = keyPre + id;
        // 从redis查询缓存
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        R result = null;
        // 缓存击穿
        if("".equals(jsonStr)){
            return null;
        }
        if(StrUtil.isNotBlank(jsonStr)){
            // 查询命中
            RedisExpireData redisExpireData = JSONUtil.toBean(jsonStr, RedisExpireData.class);
            result = JSONUtil.toBean((JSONObject) redisExpireData.getData(), type);
            // 判断是否过期
            if(redisExpireData.getExpireTime().isAfter(LocalDateTime.now())){
                // 未过期则返回
                return result;
            }
        }
        // 获取锁准备开启缓存重建
        String lockKey = lockKeyPre+id;
        try {
            if (this.tryLock(lockKey)) {
                // 交给线程池处理缓存重建逻辑
                CACHE_BUILD_EXECUTOR.execute(()->{
                    // 查询数据库
                    log.info("111111111111111111111111111");
                    R dbResult = dbFallBack.apply(id);
                    log.info("222222222222222222222222222");
                    log.info(JSONUtil.toJsonStr(dbResult));
                    // 写入缓存
                    if(null == dbResult){
                        stringRedisTemplate.opsForValue().set(key,EMPTY_DATA);
                    }else {
                        this.setWithLogicalExpire(key,dbResult,time,unit);
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("重建缓存异常");
        } finally {
            this.unLock(lockKey);
        }
        // 返回过期数据
        return result;
    }

    /**
     * 获取一批指定前缀的key eg: key:* 获取所有key:开头的key
     *
     * @param pattern key匹配正则
     * @param count   一次获取数目
     * @return
     */
    public Set<String> scan(RedisTemplate<String, Object> redisTemplate, String pattern, Integer count) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keysTmp = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(count).build())){
                while (cursor.hasNext()) {
                    keysTmp.add(new String(cursor.next(), "Utf-8"));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return keysTmp;
        });
    }

    // todo 待调整
    private boolean tryLock(String key){
        return stringRedisTemplate.opsForValue().setIfAbsent(key,"1");
    }

    private void unLock(String key){
        stringRedisTemplate.delete(key);
    }
}
