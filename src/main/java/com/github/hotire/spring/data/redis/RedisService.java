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
            final String valueString = toString(value, type);
            redisTemplate.opsForValue().set(key, valueString, timeout);
        }

        @Override
        public <T> void addToSet(String key, T value, Duration timeout, Class<T> type) {
            final String valueString = toString(value, type);
            redisTemplate.opsForSet().add(key, valueString);
            redisTemplate.expire(key, timeout.toMillis(), TimeUnit.MILLISECONDS);
        }

        private <T> String toString(final T value, final Class<T> type) {
            if (String.class.equals(type)) {
                return (String) value;
            }
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
