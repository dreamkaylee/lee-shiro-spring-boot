package com.i5018.shiro.autoconfigure;

import org.apache.shiro.event.EventBus;
import org.apache.shiro.event.support.DefaultEventBus;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.ShiroEventBusBeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author limk
 * @date 2020/8/27 17:10
 */
@Configuration
@ConditionalOnProperty(name = "shiro.enabled", matchIfMissing = true)
@AutoConfigureAfter(ShiroAutoConfiguration.class)
public class ShiroBeanAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    protected EventBus eventBus() {
        return new DefaultEventBus();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShiroEventBusBeanPostProcessor shiroEventBusAwareBeanPostProcessor() {
        return new ShiroEventBusBeanPostProcessor(eventBus());
    }

}
