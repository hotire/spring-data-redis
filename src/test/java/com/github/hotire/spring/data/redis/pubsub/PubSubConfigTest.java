package com.github.hotire.spring.data.redis.pubsub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class PubSubConfigTest {

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;

    @Test
    void pubSub() throws InterruptedException {
        redisMessagePublisher.publish("hello");
        Thread.sleep(3000L);
    }

}