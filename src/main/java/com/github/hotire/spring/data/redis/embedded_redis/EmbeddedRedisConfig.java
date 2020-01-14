package com.github.hotire.spring.data.redis.embedded_redis;


import java.io.IOException;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;


@Slf4j
@Profile("local")
@Configuration
public class EmbeddedRedisConfig implements InitializingBean, DisposableBean {

  @Value("${spring.redis.port}")
  private int redisPort;

  private RedisServer redisServer;

  @Override
  public void destroy() throws Exception {
    Optional.ofNullable(redisServer).ifPresent(RedisServer::stop);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    redisServer = new RedisServer(redisPort);
    redisServer.start();
  }
}