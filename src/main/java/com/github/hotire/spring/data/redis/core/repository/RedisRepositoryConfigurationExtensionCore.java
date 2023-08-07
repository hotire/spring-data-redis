package com.github.hotire.spring.data.redis.core.repository;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.repository.configuration.RedisRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;


/**
 * @see RedisRepositoryConfigurationExtension
 */
public class RedisRepositoryConfigurationExtensionCore {

    /**
     * @see RedisRepositoryConfigurationExtension#getDefaultKeyValueTemplateBeanDefinition(RepositoryConfigurationSource) 
     */
    protected AbstractBeanDefinition getDefaultKeyValueTemplateBeanDefinition(
        RepositoryConfigurationSource configurationSource) {
        return null;
    }
}
