package com.github.hotire.spring.data.redis.pubsub;

public interface MessagePublisher {
    void publish(String message);
}
