package com.github.hotire.spring.data.redis.embedded_redis;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Builder
@Getter
@RedisHash("point")
public class Point implements Serializable {

  @Id
  private String id;
  private Long amount;
  private LocalDateTime refreshTime;

  public void refresh(long amount, LocalDateTime refreshTime){
    if(refreshTime.isAfter(getRefreshTime())) {
      this.amount = amount;
      this.refreshTime = refreshTime;
    }
  }
}
