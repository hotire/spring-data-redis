package com.github.hotire.spring.data.redis;

import java.time.Duration;

import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;

@Configuration
public class RedisConfig {
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
