package com.github.hotire.spring.data.redis.message;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class Receiver {

  @Getter
  private final Set<String> cache = new HashSet<>();
  private final CountDownLatch latch;

  public void receiveMessage(final String message) {
    log.info("Received < {} > ", message);
    cache.add(message);
    latch.countDown();
  }
}