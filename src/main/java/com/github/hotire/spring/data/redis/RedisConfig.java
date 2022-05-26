package com.github.hotire.spring.data.redis;

import java.time.Duration;

import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplateFactory redisTemplateFactory(final RedisConnectionFactory connectionFactory, final ObjectMapper objectMapper) {
        return new RedisTemplateFactory(connectionFactory, objectMapper);
    }

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
        return cb -> cb.clientOptions(ClusterClientOptions.builder()
                                                          .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder()
                                                                                                               .refreshPeriod(Duration.ofMinutes(15))
                                                                                                               .enableAdaptiveRefreshTrigger()
                                                                                                               .build())
                                                          .build());
    }
}
