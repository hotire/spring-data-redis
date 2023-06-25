package com.github.hotire.spring.data.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @see RedisTemplate
 */
public interface RedisService {


    <T> void save(final String key, final T value, final Duration timeout, final Class<T> type);
    <T> void addToSet(final String key, final T value, final Duration timeout, final Class<T> type);


    @Service
    @RequiredArgsConstructor
    class Default implements RedisService {

        private final RedisTemplate<String, String> redisTemplate;
        private final ObjectMapper objectMapper;

        @Override
        public <T> void save(final String key, final T value, final Duration timeout, final Class<T> type) {
            if (String.class.equals(type)) {
                final String casted = (String) value;
                redisTemplate.opsForValue().set(key, casted, timeout);
                return;
            }

            try {
                final String json = objectMapper.writeValueAsString(value);
                redisTemplate.opsForValue().set(key, json, timeout);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        @Override
        public <T> void addToSet(String key, T value, Duration timeout,
            Class<T> type) {
            if (String.class.equals(type)) {
                final String casted = (String) value;
                redisTemplate.opsForSet().add(key, casted);
                redisTemplate.expire(key, timeout.toMillis(), TimeUnit.MILLISECONDS);
                return;
            }

            try {
                final String json = objectMapper.writeValueAsString(value);
                redisTemplate.opsForSet().add(key, json);
                redisTemplate.expire(key, timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
