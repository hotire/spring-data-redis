package com.github.hotire.spring.data.redis.core.pubsub;

import java.util.concurrent.Executor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * @see RedisMessageListenerContainer
 */
public class RedisMessageListenerContainerCore {

    public static final String DEFAULT_THREAD_NAME_PREFIX = ClassUtils.getShortName(RedisMessageListenerContainer.class);

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
}
