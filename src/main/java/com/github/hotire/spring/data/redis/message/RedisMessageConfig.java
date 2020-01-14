package com.github.hotire.spring.data.redis.message;

import java.util.concurrent.CountDownLatch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;


@Configuration
public class RedisMessageConfig {

  public static final String TOPIC = "chat";

  @Bean
  public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
    MessageListenerAdapter listenerAdapter) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(listenerAdapter, new PatternTopic(TOPIC));

    return container;
  }

  @Bean
  public MessageListenerAdapter listenerAdapter(Receiver receiver) {
    return new MessageListenerAdapter(receiver, "receiveMessage");
  }

  @Bean
  public Receiver receiver(CountDownLatch latch) {
    return new Receiver(latch);
  }

  @Bean
  public CountDownLatch latch() {
    return new CountDownLatch(1);
  }

  @Bean
  public StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }
}
