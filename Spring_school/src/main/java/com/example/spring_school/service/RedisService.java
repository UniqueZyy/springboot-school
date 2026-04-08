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
                // Redis不可用时，忽略异常
                System.err.println("Redis set operation failed: " + e.getMessage());
            }
        }
    }

    // 获取缓存
    public Object get(String key) {
        if (key != null) {
            try {
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                // Redis不可用时，返回null
                System.err.println("Redis get operation failed: " + e.getMessage());
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
                // Redis不可用时，忽略异常
                System.err.println("Redis delete operation failed: " + e.getMessage());
            }
        }
    }

    // 缓存是否存在
    public boolean exists(String key) {
        if (key != null) {
            try {
                return redisTemplate.hasKey(key);
            } catch (Exception e) {
                // Redis不可用时，返回false
                System.err.println("Redis exists operation failed: " + e.getMessage());
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
                // Redis不可用时，返回null
                System.err.println("Redis increment operation failed: " + e.getMessage());
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
                // Redis不可用时，返回null
                System.err.println("Redis decrement operation failed: " + e.getMessage());
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
                // Redis不可用时，忽略异常
                System.err.println("Redis set operation failed: " + e.getMessage());
            }
        }
    }
}
