package com.example.spring_school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 设置缓存
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        if (key != null && value != null && unit != null) {
            try {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            } catch (Exception e) {
                System.err.println("Redis set 失败：" + e.getMessage());
            }
        }
    }

    // 获取缓存
    public Object get(String key) {
        if (key != null) {
            try {
                // 增加超时控制，防止连接池卡住
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                // 强制打印日志，确保你能看到
                System.err.println("Redis 已关闭/挂掉 → 强制返回null，走数据库保底");
                return null;
            }
        }
        return null;
    }

    // 删除缓存
    public void delete(String key) {
        if (key != null) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                System.err.println("Redis delete 失败：" + e.getMessage());
            }
        }
    }

    // 缓存是否存在
    public boolean exists(String key) {
        if (key != null) {
            try {
                return redisTemplate.hasKey(key);
            } catch (Exception e) {
                System.err.println("Redis exists 失败：" + e.getMessage());
                return false;
            }
        }
        return false;
    }

    // 自增
    public Long increment(String key, long delta) {
        if (key != null) {
            try {
                return redisTemplate.opsForValue().increment(key, delta);
            } catch (Exception e) {
                System.err.println("Redis increment 失败：" + e.getMessage());
                return null;
            }
        }
        return null;
    }

    // 自减
    public Long decrement(String key, long delta) {
        if (key != null) {
            try {
                return redisTemplate.opsForValue().decrement(key, delta);
            } catch (Exception e) {
                System.err.println("Redis decrement 失败：" + e.getMessage());
                return null;
            }
        }
        return null;
    }

    // 设置缓存（无过期时间）
    public void set(String key, Object value) {
        if (key != null && value != null) {
            try {
                redisTemplate.opsForValue().set(key, value);
            } catch (Exception e) {
                System.err.println("Redis set 失败：" + e.getMessage());
            }
        }
    }
}