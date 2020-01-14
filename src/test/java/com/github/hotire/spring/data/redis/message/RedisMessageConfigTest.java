package com.github.hotire.spring.data.redis.message;

import static com.github.hotire.spring.data.redis.message.RedisMessageConfig.TOPIC;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@SpringBootTest
class RedisMessageConfigTest {

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @Autowired
  private CountDownLatch countDownLatch;

  @Autowired
  private Receiver receiver;

  @Test
  void convertAndSend() throws InterruptedException {
    // given
    final String message = "Hello from Redis!";
    log.info("Sending message...");

    // when
    stringRedisTemplate.convertAndSend(TOPIC, message);

    // then
    countDownLatch.await(1, TimeUnit.MINUTES);
    assertThat(receiver.getCache()).contains(message);
  }
}