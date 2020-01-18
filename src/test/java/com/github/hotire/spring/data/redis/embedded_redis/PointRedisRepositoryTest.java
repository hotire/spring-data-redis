package com.github.hotire.spring.data.redis.embedded_redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;

@Import(EmbeddedRedisConfig.class)
@DataRedisTest
class PointRedisRepositoryTest {

  @Autowired
  private PointRedisRepository pointRedisRepository;

  @Test
  void save_find() {
    //given
    final String id = "hotire";
    final LocalDateTime refreshTime = LocalDateTime.of(2018, 5, 26, 0, 0);


    //when
    pointRedisRepository.save(Point.builder()
                                   .id(id)
                                   .amount(1000L)
                                   .refreshTime(refreshTime)
                                   .build());

    final Point savedPoint = pointRedisRepository.findById(id).orElseThrow();

    // then
    assertThat(savedPoint.getAmount()).isEqualTo(1000L);
    assertThat(savedPoint.getRefreshTime()).isEqualTo(refreshTime);
  }

}