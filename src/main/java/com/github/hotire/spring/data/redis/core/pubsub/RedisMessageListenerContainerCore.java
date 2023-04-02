package com.github.hotire.spring.data.redis.core.pubsub;

import java.util.Collection;
import java.util.concurrent.Executor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * @see RedisMessageListenerContainer
 */
public class RedisMessageListenerContainerCore {

    /**
     * @see RedisMessageListenerContainer#DEFAULT_THREAD_NAME_PREFIX
     */
    public static final String DEFAULT_THREAD_NAME_PREFIX = ClassUtils.getShortName(RedisMessageListenerContainer.class);

    /**
     * @see RedisMessageListenerContainer#beanName
     */
    private @Nullable String beanName;

    /**
     * @see RedisMessageListenerContainer#taskExecutor
     */
    private @Nullable Executor taskExecutor;


    /**
     * @see RedisMessageListenerContainer#createDefaultTaskExecutor()
     */
    protected TaskExecutor createDefaultTaskExecutor() {
        String threadNamePrefix = (beanName != null ? beanName + "-" : DEFAULT_THREAD_NAME_PREFIX);
        return new SimpleAsyncTaskExecutor(threadNamePrefix);
    }

    /**
     * @see RedisMessageListenerContainer#addMessageListener(MessageListener, Topic) 
     */
    public void addMessageListener(MessageListener listener, Collection<? extends Topic> topics) {
        
    }
}
