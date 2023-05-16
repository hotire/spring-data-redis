package com.github.hotire.spring.data.redis.core;

import java.util.HashSet;
import java.util.Set;
import org.springframework.data.redis.core.SetOperations;

/**
 * @see SetOperations
 */
public class SetOperationsCore<K, V> {

    /**
     * @see SetOperations#add(Object, Object[])
     */
    Long add(K key, V... values) {
        return 0L;
    }

    /**
     * @see SetOperations#members(Object)
     */
    Set<V> members(K key) {
        return new HashSet<>();
    }
}
