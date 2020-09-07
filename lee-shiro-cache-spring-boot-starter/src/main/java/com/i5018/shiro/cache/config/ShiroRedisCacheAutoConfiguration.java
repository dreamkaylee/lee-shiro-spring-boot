package com.i5018.shiro.cache.config;

import com.i5018.shiro.cache.ExpiredCacheManager;
import com.i5018.shiro.cache.config.properties.ShiroCacheProperties;
import com.i5018.shiro.cache.RedisCacheManager;
import com.i5018.shiro.session.RedisSessionDAO;
import com.i5018.shiro.session.repository.SessionRepository;
import com.i5018.shiro.session.repository.SessionRepositoryImpl;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author limk
 * @date 2020/8/28 9:19
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "shiro.cache.redis.enabled", matchIfMissing = true)
@EnableConfigurationProperties(ShiroCacheProperties.class)
@Import(RedisAutoConfiguration.class)
public class ShiroRedisCacheAutoConfiguration {

    private final ShiroCacheProperties properties;

    public ShiroRedisCacheAutoConfiguration(ShiroCacheProperties properties) {
        this.properties = properties;
    }

    @Bean("shiroCacheManager")
    @ConditionalOnMissingBean
    public ExpiredCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheManager cacheManager = new RedisCacheManager(connectionFactory);
        cacheManager.setKeyPrefix(properties.getKeyPrefix());
        cacheManager.setExpiration(properties.getExpiration());
        return cacheManager;
    }

    @Bean("redisSessionDAO")
    @ConditionalOnMissingBean
    public SessionDAO sessionDAO(ExpiredCacheManager cacheManager, RedisConnectionFactory connectionFactory) {
        RedisSessionDAO sessionDAO = new RedisSessionDAO(shiroSessionRepository(connectionFactory));
        sessionDAO.setCacheManager(cacheManager);
        return sessionDAO;
    }

    @Bean
    public SessionRepository shiroSessionRepository(RedisConnectionFactory connectionFactory) {
        return new SessionRepositoryImpl(connectionFactory);
    }

}
