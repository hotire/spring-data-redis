package com.github.hotire.spring.data.redis.pubsub;

import com.github.hotire.spring.data.redis.RedisTemplateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class PubSubConfig {

    @Bean
    public RedisTemplate<String, Object> stringObjectRedisTemplate(final RedisTemplateFactory redisTemplateFactory) {
        return redisTemplateFactory.getRedisTemplate(Object.class);
    }

    @Bean
    public RedisMessagePublisher redisMessagePublisher(final RedisTemplate<String, Object> redisTemplate) {
        return new RedisMessagePublisher(redisTemplate, topic());
    }

    @Bean
    public MessageListenerAdapter messageListener() {
        return new MessageListenerAdapter(new RedisMessageSubscriber());
    }

    @Bean
    public MessageListener messageListenerBean() {
        return new RedisMessageSubscriber();
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container
                = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(messageListenerBean(), topic());
        return container;
    }

    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("pubsub:queue");
    }
}
