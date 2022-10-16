package com.hmdp.service.impl;

import cn.hutool.core.lang.UUID;
import com.hmdp.service.ILock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock {
    private final String PRE_LOCK_KEY = "lock:";
    private final String THREAD_ID = UUID.randomUUID().toString(true)+"-";
    // 业务名称
    private final String name;
    private StringRedisTemplate stringRedisTemplate;
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }


    public SimpleRedisLock(String name, StringRedisTemplate redisTemplate) {
        this.name = name;
        this.stringRedisTemplate = redisTemplate;
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        String key = PRE_LOCK_KEY + name;
        String value = THREAD_ID + "-" + Thread.currentThread().getId();
        return stringRedisTemplate.opsForValue().setIfAbsent(key,value,timeoutSec, TimeUnit.SECONDS);
    }

    @Override
    public void unlock() {
        // 调用lua脚本
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(PRE_LOCK_KEY + name),
                Collections.singleton(THREAD_ID + "-" + Thread.currentThread().getId())
        );

    }

//    @Override
//    public void unlock() {
//        String key = preLock+name;
//        String value = id + "-" + Thread.currentThread().getId();
//        // 防止误删
//        /**
//         * 缺点：判断与删除非原子性，还是有可能删除其他线程的锁
//         */
//        if(value.equals(redisTemplate.opsForValue().get(key))){
//            redisTemplate.delete(preLock+name);
//        }
//    }
}
