package com.github.hotire.spring.data.redis.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;


class ReceiverTest {

  @Test
  void receiveMessage() {
    // given
    final String message = "test";
    Receiver receiver = new Receiver(new CountDownLatch(1));

    // when
    receiver.receiveMessage(message);

    // then
    assertThat(receiver.getCache()).contains(message);
  }
}