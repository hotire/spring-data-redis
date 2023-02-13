package com.github.hotire.spring.data.redis.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ZSetOperations;

@SpringBootTest
class ZSetOperationsCoreTest {

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSetOperations;

    @Test
    void range() {
        // given
        final String key = "key";
        zSetOperations.add(key, "AAA", System.currentTimeMillis());
        zSetOperations.add(key, "BBB", System.currentTimeMillis());
        zSetOperations.add(key, "CCC", System.currentTimeMillis());

        // when
        final Set<String> set = zSetOperations.range("key", 0, 0);

        // then
        assertThat(set).contains("AAA");
    }

    @Test
    void addDuplicatedValue() {
        // given
        final String key = "key";
        zSetOperations.add(key, "AAA", System.currentTimeMillis());

        // when
        final Boolean result = zSetOperations.add(key, "AAA", System.currentTimeMillis());

        // then
        assertThat(result).isFalse();
    }

}