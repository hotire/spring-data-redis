package com.github.hotire.spring.data.redis.core;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @see RedisTemplate
 */
public class RedisTemplateCore {

    /**
     * @see RedisTemplate#expire(Object, long, TimeUnit)
     */
    public <K> Boolean expire(K key, final long timeout, final TimeUnit unit) {
        return true;
    }
}
