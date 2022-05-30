package com.github.hotire.spring.data.redis.pubsub;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.support.RetryTemplate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RetryingRedisMessageListenerAdapter implements MessageListener {

    private final MessageListener delegate;
    private final RetryTemplate retryTemplate;
    private final RecoveryCallback<? extends Object> recoveryCallback;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        retryTemplate.execute(context -> {
            delegate.onMessage(message, pattern);
            return null;
        }, recoveryCallback);
    }
}
