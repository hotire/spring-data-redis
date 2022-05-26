package com.github.hotire.spring.data.redis;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisTemplateFactory {

    @Getter
    private final RedisConnectionFactory redisConnectionFactory;
    private final ObjectMapper objectMapper;
    private final StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();


    public RedisTemplate<String, String> getRedisTemplate() {
        return getRedisTemplate(stringRedisSerializer, stringRedisSerializer);
    }

    public <V> RedisTemplate<String, V> getRedisTemplate(Class<V> clazz) {
        return getRedisTemplate(stringRedisSerializer, createRedisSerializer(clazz));
    }

    public <K, V> RedisTemplate<K, V> getRedisTemplate(Class<K> keyClass,
                                                       Class<V> valueClass) {
        RedisSerializer<K> keySerializer = createRedisSerializer(keyClass);
        RedisSerializer<V> valueSerializer = createRedisSerializer(valueClass);

        return getRedisTemplate(keySerializer, valueSerializer);
    }

    public <K, V> RedisTemplate<K, V> getRedisTemplate(RedisSerializer<K> keySerializer, RedisSerializer<V> valueSerializer) {
        RedisTemplate<K, V> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private <V> RedisSerializer<V> createRedisSerializer(final Class<V> clazz) {
        return createRedisSerializer(clazz, "JACKSON");
    }
    private <V> RedisSerializer<V> createRedisSerializer(final Class<V> clazz, final String serializerType) {
        switch (serializerType) {
            case "JDK":
                return (RedisSerializer<V>) new JdkSerializationRedisSerializer();
            case "JACKSON":
                Jackson2JsonRedisSerializer<V> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(clazz);
                jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
                return jackson2JsonRedisSerializer;
            default:
                return (RedisSerializer<V>) stringRedisSerializer;
        }
    }
}
