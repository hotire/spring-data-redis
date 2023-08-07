package com.github.hotire.spring.data.redis.core.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.redis.repository.support.RedisRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

/**
 * @see KeyValueRepositoryFactory
 * @see RedisRepositoryFactory
 * @see RepositoryFactorySupport
 */
@RequiredArgsConstructor
public class KeyValueRepositoryFactoryCore {

    /**
     * @see KeyValueRepositoryFactory#keyValueOperations
     */
    private final KeyValueOperations keyValueOperations;

}
