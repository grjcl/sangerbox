package com.video.server.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author : zp
 * @Description :
 * @Date : 2019/10/17
 */
@Service
public class RedisConfig {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 默认过期时长，单位：秒
     */
    public static final long DEFAULT_EXPIRE = 60 * 60 * 24;

    /**
     * 不设置过期时长
     */
    public static final long NOT_EXPIRE = -1;

    public boolean setKey(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        return true;
    }

    public boolean setExpireKey(String key, Object value, Long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.MINUTES);
        return true;
    }

    public Object getKey(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean existsKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 重名名key，如果newKey已经存在，则newKey的原值被覆盖
     *
     * @param oldKey
     * @param newKey
     */
    public void renameKey(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * newKey不存在时才重命名
     *
     * @param oldKey
     * @param newKey
     * @return 修改成功返回true
     */
    public boolean renameKeyNotExist(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(oldKey, newKey);
    }

    /**
     * 删除key
     *
     * @param key
     */
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 删除多个key
     *
     * @param keys
     */
    public void deleteKey(String... keys) {
        Set<String> kSet = Stream.of(keys).map(k -> k).collect(Collectors.toSet());
        redisTemplate.delete(kSet);
    }

    /**
     * 删除Key的集合
     *
     * @param keys
     */
    public void deleteKey(Collection<String> keys) {
        Set<String> kSet = keys.stream().map(k -> k).collect(Collectors.toSet());
        redisTemplate.delete(kSet);
    }

    /**
     * 设置key的生命周期
     *
     * @param key
     * @param time
     * @param timeUnit
     */
    public void expireKey(String key, long time, TimeUnit timeUnit) {
        redisTemplate.expire(key, time, timeUnit);
    }

    /**
     * 指定key在指定的日期过期
     *
     * @param key
     * @param date
     */
    public void expireKeyAt(String key, Date date) {
        redisTemplate.expireAt(key, date);
    }

    /**
     * 查询key的生命周期
     *
     * @param key
     * @param timeUnit
     * @return
     */
    public long getKeyExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 将key设置为永久有效
     *
     * @param key
     */
    public void persistKey(String key) {
        redisTemplate.persist(key);
    }

    public void setHashKey(String key, String feid, String val) {
        redisTemplate.opsForHash().put(key, feid, val);
    }

    public static final String LOCK_PREFIX = "redis_lock_";
    //加锁失效时间，毫秒
    public static final int LOCK_EXPIRE = 300;

    public boolean redLock(String key, String val, long timeout, TimeUnit timeUnit) {
        key = LOCK_PREFIX + key;
        while (redisTemplate.hasKey(key)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        redisTemplate.opsForValue().setIfAbsent(key, val, timeout, timeUnit);
        return true;
    }

    public Boolean rmLock(String key, String val) {
        key = LOCK_PREFIX + key;
        String name = Thread.currentThread().getName();
        if (val.equals(name)) {
            return redisTemplate.delete(key);
        }
        return false;
    }

}


