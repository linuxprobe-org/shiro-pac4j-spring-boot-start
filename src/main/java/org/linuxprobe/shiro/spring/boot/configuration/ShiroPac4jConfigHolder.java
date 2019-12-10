package org.linuxprobe.shiro.spring.boot.configuration;

import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.linuxprobe.luava.cache.impl.redis.RedisCache;
import org.linuxprobe.luava.shiro.redis.session.ShiroRedisSessionDAO;
import org.linuxprobe.shiro.session.SessionTokenStore;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;

import javax.servlet.Filter;
import java.util.Map;


@Getter
@Setter
public class ShiroPac4jConfigHolder {
    private RedisCache redisCache;
    private ShiroPac4jProperties shiroProperties;
    private Config config;
    private SessionTokenStore sessionTokenStore;
    private Clients clients;
    private ShiroRedisSessionDAO sessionDAO;
    private SessionManager sessionManager;
    private SecurityManager securityManager;
    private AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor;
    private Map<String, Filter> filters;
    private ShiroFilterFactoryBean shiroFilterFactoryBean;
}
