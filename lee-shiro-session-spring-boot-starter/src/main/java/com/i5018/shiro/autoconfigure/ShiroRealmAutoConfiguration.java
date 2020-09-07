package com.i5018.shiro.autoconfigure;

import com.i5018.shiro.authc.UserPrincipalService;
import com.i5018.shiro.authc.UserPrincipalSmsService;
import com.i5018.shiro.authc.credential.RetryLimitCredentialsMatcher;
import com.i5018.shiro.authc.realm.SmsRealm;
import com.i5018.shiro.authc.realm.UsernamePasswordRealm;
import com.i5018.shiro.autoconfigure.properties.ShiroProperties;
import com.i5018.shiro.cache.ExpiredCacheManager;
import com.i5018.shiro.cache.config.ShiroRedisCacheAutoConfiguration;
import org.apache.shiro.realm.Realm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author limk
 * @date 2020/8/27 8:54
 */
@Configuration
@EnableConfigurationProperties(ShiroProperties.class)
@AutoConfigureAfter(ShiroRedisCacheAutoConfiguration.class)
public class ShiroRealmAutoConfiguration {

    @Autowired
    private ShiroProperties properties;
    @Autowired
    private ExpiredCacheManager cacheManager;
    @Autowired(required = false)
    private UserPrincipalService userPrincipalService;
    @Autowired(required = false)
    private UserPrincipalSmsService userPrincipalSmsService;

    @Bean("usernamePasswordRealm")
    public Realm usernamePasswordRealm() {
        return new UsernamePasswordRealm(cacheManager, credentialsMatcher(), userPrincipalService);
    }

    @Bean("smsRealm")
    public Realm smsRealm() {
        return new SmsRealm(cacheManager, userPrincipalSmsService);
    }

    @Bean
    public RetryLimitCredentialsMatcher credentialsMatcher() {
        RetryLimitCredentialsMatcher credentialsMatcher = new RetryLimitCredentialsMatcher(cacheManager);
        credentialsMatcher.setHashAlgorithmName(properties.getCredential().getHashAlgorithm());
        credentialsMatcher.setHashIterations(properties.getCredential().getHashIterations());
        credentialsMatcher.setStoredCredentialsHexEncoded(properties.getCredential().isStoredCredentialsHexEncoded());
        credentialsMatcher.setMaxErrorsCount(properties.getCredential().getErrorsCount());
        credentialsMatcher.setExpiration(properties.getCredential().getExpiration());
        return credentialsMatcher;
    }

}
