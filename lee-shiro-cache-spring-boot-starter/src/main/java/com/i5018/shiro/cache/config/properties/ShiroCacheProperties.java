package com.i5018.shiro.cache.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author limk
 * @date 2020/8/28 9:27
 */
@Data
@ConfigurationProperties(prefix = "shiro.cache")
public class ShiroCacheProperties {

    private String keyPrefix = "shiro:cache:";

    private Duration expiration = Duration.ofMinutes(30);

    private RedisCacheProperties redis = new RedisCacheProperties();

    @Data
    private static class RedisCacheProperties {

        private boolean enabled = false;

    }

}
