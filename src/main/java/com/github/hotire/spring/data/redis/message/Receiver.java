package com.github.hotire.spring.data.redis.message;

import java.util.concurrent.CountDownLatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class Receiver {

  private final CountDownLatch latch;

  public void receiveMessage(final String message) {
    log.info("Received <" + message + ">");
    latch.countDown();
  }
}