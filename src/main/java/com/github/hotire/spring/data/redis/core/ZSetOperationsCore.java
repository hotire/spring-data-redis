package com.github.hotire.spring.data.redis.core;

import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

/**
 * @see org.springframework.data.redis.core.ZSetOperations
 */
@Slf4j
@Component
public class ZSetOperationsCore {

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSetOperations;

}
