package com.i5018.shiro.autoconfigure;

import com.i5018.shiro.authc.realm.MyModularRealmAuthenticator;
import com.i5018.shiro.autoconfigure.properties.ShiroProperties;
import com.i5018.shiro.cache.ExpiredCacheManager;
import com.i5018.shiro.filter.AsyncFormAuthenticationFilter;
import com.i5018.shiro.filter.KickOutSessionControlFilter;
import com.i5018.shiro.session.ShiroSessionFactory;
import com.i5018.shiro.session.ShiroSessionManager;
import com.i5018.shiro.session.listener.KickOutSessionListener;
import com.i5018.shiro.session.listener.ShiroSessionListener;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author limk
 * @date 2020/8/26 9:27
 */
@Configuration
@AutoConfigureAfter(ShiroRealmAutoConfiguration.class)
public class ShiroAutoConfiguration {

    @Autowired
    private ShiroProperties properties;
    @Autowired
    private ExpiredCacheManager cacheManager;
    @Autowired
    private SessionDAO sessionDAO;
    @Autowired
    private List<Realm> realms;

    @Bean
    @ConditionalOnMissingBean(SessionDAO.class)
    public SessionDAO sessionDAO() {
        return new EnterpriseCacheSessionDAO();
    }

    @Bean("sessionFactory")
    public ShiroSessionFactory sessionFactory() {
        return new ShiroSessionFactory();
    }

    @Bean
    public SessionManager sessionManager() {
        ShiroSessionManager sessionManager = new ShiroSessionManager();
        sessionManager.setSessionIdCookieEnabled(true);
        sessionManager.setSessionDAO(sessionDAO);
        sessionManager.setCacheManager(cacheManager);
        sessionManager.setSessionFactory(sessionFactory());
        sessionManager.setGlobalSessionTimeout(properties.getSession().getSessionExpire());
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setSessionValidationInterval(properties.getSession().getSessionValidationInterval());
        sessionManager.setSessionIdUrlRewritingEnabled(false);

        sessionManager.setSessionIdCookie(new SimpleCookie(properties.getCookie().getCookieName()));
        sessionManager.setSessionListeners(Arrays.asList(shiroSessionListener(), kickOutSessionListener()));
        return sessionManager;
    }

    @Bean
    public RememberMeManager cookieRememberMeManager() {
        CookieRememberMeManager rememberMeManager = new CookieRememberMeManager();
        rememberMeManager.setCookie(rememberMeCookie());
        return rememberMeManager;
    }

    @Bean("rememberMeCookie")
    public SimpleCookie rememberMeCookie() {
        SimpleCookie simpleCookie = new SimpleCookie(properties.getCookie().getCookieName());
        simpleCookie.setMaxAge(properties.getCookie().getMaxAge());
        if (properties.getCookie().getDomain() != null) {
            simpleCookie.setDomain(properties.getCookie().getDomain());
        }
        return simpleCookie;
    }

    @Bean
    public MyModularRealmAuthenticator modularRealmAuthenticator() {
        MyModularRealmAuthenticator authenticator = new MyModularRealmAuthenticator();
        authenticator.setRealms(realms);
        authenticator.setAuthenticationStrategy(new FirstSuccessfulStrategy());
        return authenticator;
    }

    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealms(realms);
        securityManager.setAuthenticator(modularRealmAuthenticator());
        securityManager.setSessionManager(sessionManager());
        securityManager.setCacheManager(cacheManager);
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager());
        shiroFilterFactoryBean.setLoginUrl(properties.getFilter().getLoginUrl());
        shiroFilterFactoryBean.setSuccessUrl(properties.getFilter().getSuccessUrl());
        LinkedHashMap<String, Filter> filtersMap = new LinkedHashMap<>();
        filtersMap.put("kick", kickOutSessionControlFilter());
        filtersMap.put("authc", new AsyncFormAuthenticationFilter());
        shiroFilterFactoryBean.setFilters(filtersMap);

        // 过滤链定义，从上向下顺序执行，一般将 /**放在最为下边
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        List<String> anons = properties.getFilter().getAnon();
        if (!CollectionUtils.isEmpty(anons)) {
            anons.forEach(ignored -> filterChainDefinitionMap.put(ignored, "anon"));
        }
        filterChainDefinitionMap.put("/**", "kick, authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    @Bean
    public KickOutSessionControlFilter kickOutSessionControlFilter() {
        KickOutSessionControlFilter filter = new KickOutSessionControlFilter(cacheManager, sessionManager());
        filter.setKickOutAfter(properties.getKickOut().isKickOutAfter());
        filter.setMaxSession(properties.getKickOut().getMaxSession());
        filter.setKickOutUrl(properties.getKickOut().getKickOutUrl());
        return filter;
    }

    @Bean
    @ConditionalOnBean(SessionDAO.class)
    public ShiroSessionListener shiroSessionListener() {
        return new ShiroSessionListener(sessionDAO);
    }

    @Bean
    @ConditionalOnBean(CacheManager.class)
    public KickOutSessionListener kickOutSessionListener() {
        return new KickOutSessionListener(cacheManager);
    }

    @Bean
    public MethodInvokingFactoryBean getMethodInvokingFactoryBean() {
        MethodInvokingFactoryBean factoryBean = new MethodInvokingFactoryBean();
        factoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
        factoryBean.setArguments(securityManager());
        return factoryBean;
    }

}
