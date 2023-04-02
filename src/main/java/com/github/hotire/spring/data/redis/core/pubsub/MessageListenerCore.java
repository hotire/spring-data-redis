package com.github.hotire.spring.data.redis.core.pubsub;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.Nullable;

/**
 * @see MessageListener
 */
public class MessageListenerCore {

    /**
     * @see MessageListener#onMessage(Message, byte[]) 
     */
    void onMessage(Message message, @Nullable byte[] pattern) {
        
    }

}
