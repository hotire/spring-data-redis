package com.github.hotire.spring.data.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @see RedisTemplate
 */
public interface RedisService {


    <T> void save(String key, T value);


    @Service
    @RequiredArgsConstructor
    class Default implements RedisService {

        private final RedisTemplate<String, String> redisTemplate;

        @Override
        public <T> void save(String key, T value) {

        }
    }
}
