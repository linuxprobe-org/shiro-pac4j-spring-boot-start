package org.linuxprobe.shiro.spring.boot.configuration;

import io.buji.pac4j.context.ShiroSessionStore;
import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.LogoutFilter;
import io.buji.pac4j.subject.Pac4jSubjectFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.linuxprobe.luava.cache.impl.redis.RedisCache;
import org.linuxprobe.luava.shiro.redis.cache.ShiroRedisCacheManager;
import org.linuxprobe.luava.shiro.redis.session.ShiroRedisSessionDAO;
import org.linuxprobe.shiro.filter.*;
import org.linuxprobe.shiro.pac4j.authc.ClientAuthorizationInfo;
import org.linuxprobe.shiro.realm.Pac4jShiroRealm;
import org.linuxprobe.shiro.session.DefaultSessionTokenStore;
import org.linuxprobe.shiro.session.Pac4jShiroRedisSessionDAO;
import org.linuxprobe.shiro.session.Pac4jWebSessionManager;
import org.linuxprobe.shiro.session.SessionTokenStore;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnClass({ShiroPac4jProperties.class})
@ConditionalOnBean(value = {ClientAuthorizationInfo.class})
@EnableConfigurationProperties(ShiroPac4jProperties.class)
@AutoConfigureAfter({ShiroPac4jProperties.class, RedisAutoConfiguration.class})
public class ShiroPac4jAutoConfiguration implements BeanFactoryAware {
    private static Logger logger = LoggerFactory.getLogger(ShiroPac4jAutoConfiguration.class);
    private BeanFactory beanFactory;
    private RedisCache redisCache;
    private ShiroPac4jProperties shiroProperties;
    private ClientAuthorizationInfo clientAuthorizationInfo;
    private ShiroPac4jConfigHolder shiroPac4jConfigHolder;

    public ShiroPac4jAutoConfiguration(ShiroPac4jProperties shiroProperties, ClientAuthorizationInfo clientAuthorizationInfo) {
        this.shiroProperties = shiroProperties;
        this.clientAuthorizationInfo = clientAuthorizationInfo;
    }

    private ShiroPac4jConfigurationAdvice getConfigurationAdvice() {
        try {
            return this.beanFactory.getBean(ShiroPac4jConfigurationAdvice.class);
        } catch (BeansException e) {
            return new ShiroPac4jConfigurationAdvice() {
            };
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        RedisTemplate redisTemplate = beanFactory.getBean("redisTemplate", RedisTemplate.class);
        this.redisCache = new RedisCache(redisTemplate);
    }

    @PostConstruct
    public void init() {
        ShiroPac4jConfigurationAdvice configurationAdvice = this.getConfigurationAdvice();
        this.shiroPac4jConfigHolder = new ShiroPac4jConfigHolder();
        this.shiroPac4jConfigHolder.setShiroProperties(this.shiroProperties);
        this.shiroPac4jConfigHolder.setRedisCache(this.redisCache);
        configurationAdvice.initBefore(this.shiroProperties);
        // 1. pac4j config bean
        Config config = new Config("/callback");
        config.setCallbackLogic(new DefaultCallbackLogic());
        config.setSessionStore(ShiroSessionStore.INSTANCE);
        if (this.shiroProperties.getMatchers() != null) {
            config.setMatchers(this.shiroProperties.getMatchers());
        }
        // 2. sessionTokenStore bean
        SessionTokenStore sessionTokenStore = new DefaultSessionTokenStore(this.redisCache);
        this.shiroPac4jConfigHolder.setSessionTokenStore(sessionTokenStore);
        // 3. pac4j client
        Clients clients = new Clients();
        clients.setClients(configurationAdvice.getClients(this.shiroPac4jConfigHolder));
        config.setClients(clients);
        this.shiroPac4jConfigHolder.setClients(clients);
        this.shiroPac4jConfigHolder.setConfig(config);
        // 4. session dao bean
        ShiroRedisSessionDAO sessionDAO = new Pac4jShiroRedisSessionDAO(this.redisCache, sessionTokenStore);
        this.shiroPac4jConfigHolder.setSessionDAO(sessionDAO);
        // 5. session manager
        Pac4jWebSessionManager sessionManager = new Pac4jWebSessionManager(sessionTokenStore, config);
        sessionManager.setSessionDAO(sessionDAO);
        sessionManager.setGlobalSessionTimeout(this.shiroProperties.getSessionTimeout() * 1000);
        // 是否启用会话调度
        sessionManager.setSessionValidationSchedulerEnabled(this.shiroProperties.getEnableSession());
        SimpleCookie simpleCookie = new SimpleCookie();
        simpleCookie.setName("shrio_sesssion_id");
        simpleCookie.setPath("/");
        sessionManager.setSessionIdCookie(simpleCookie);
        this.shiroPac4jConfigHolder.setSessionManager(sessionManager);
        // 6. securityManager bean
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        List<Realm> realms = new ArrayList<>(3);
        realms.add(new Pac4jShiroRealm(this.clientAuthorizationInfo, this.shiroProperties.getCachePrefix()));
        realms.addAll(configurationAdvice.getRealms(this.shiroPac4jConfigHolder));
        securityManager.setRealms(realms);
        securityManager.setSessionManager(sessionManager);
        securityManager.setCacheManager(new ShiroRedisCacheManager(this.redisCache));
        securityManager.setSubjectFactory(new Pac4jSubjectFactory() {
            @Override
            public Subject createSubject(SubjectContext context) {
                // 是否创建session
                context.setSessionCreationEnabled(ShiroPac4jAutoConfiguration.this.shiroProperties.getEnableSession());
                return super.createSubject(context);
            }
        });
        DefaultSubjectDAO subjectDAO = (DefaultSubjectDAO) securityManager.getSubjectDAO();
        DefaultSessionStorageEvaluator sessionStorageEvaluator = (DefaultSessionStorageEvaluator) subjectDAO.getSessionStorageEvaluator();
        // 是否启用session存储
        sessionStorageEvaluator.setSessionStorageEnabled(this.shiroProperties.getEnableSession());
        SecurityUtils.setSecurityManager(securityManager);
        this.shiroPac4jConfigHolder.setSecurityManager(securityManager);
        // 7 aop
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        this.shiroPac4jConfigHolder.setAuthorizationAttributeSourceAdvisor(authorizationAttributeSourceAdvisor);
        // 8. filter map 自定义shiro拦截器
        Map<String, Filter> filters = new HashMap<>();
        this.shiroPac4jConfigHolder.setFilters(filters);
        filters.put(AuthcFilter.name, new AuthcFilter());
        filters.put(ShiroOriginFilter.name, new ShiroOriginFilter());
        Pac4jSecurityFilter securityFilter = new Pac4jSecurityFilter(configurationAdvice.getSecurityLogic(this.shiroPac4jConfigHolder));
        securityFilter.setConfig(config);
        filters.put(Pac4jSecurityFilter.name, securityFilter);
        CallbackFilter callbackFilter = new CallbackFilter();
        callbackFilter.setConfig(config);
        callbackFilter.setSaveInSession(true);
        callbackFilter.setDefaultUrl(this.shiroProperties.getLoginDefaultUrl());
        callbackFilter.setCallbackLogic(configurationAdvice.getCallbackLogic(this.shiroPac4jConfigHolder));
        filters.put("callback", callbackFilter);
        LogoutFilter logoutFilter = new LogoutFilter();
        logoutFilter.setConfig(config);
        logoutFilter.setDefaultUrl(this.shiroProperties.getLogoutDefaultUrl());
        logoutFilter.setLogoutLogic(configurationAdvice.getLogoutLogic(this.shiroPac4jConfigHolder));
        filters.put("logout", logoutFilter);
        filters.put(HeartbeatRequestFilter.name, new HeartbeatRequestFilter());
        filters.put(RedirectionFilter.name, new RedirectionFilter(this.shiroProperties));
        filters.putAll(configurationAdvice.getFilter(this.shiroPac4jConfigHolder));
        // 9. filter bean
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setLoginUrl(this.shiroProperties.getLoginUrl());
        shiroFilterFactoryBean.setUnauthorizedUrl(null);
        shiroFilterFactoryBean.setSuccessUrl(null);
        // 设置shiro security管理器
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setFilters(filters);
        ShiroPac4jAutoConfiguration.logger.debug("配置拦截器的url" + this.shiroProperties.getFilterChainDefinitions());
        //拦截器链路给引用方配置
        shiroFilterFactoryBean.setFilterChainDefinitionMap(this.shiroProperties.getFilterChainDefinitions());
        shiroFilterFactoryBean.setLoginUrl(this.shiroProperties.getLoginUrl());
        this.shiroPac4jConfigHolder.setShiroFilterFactoryBean(shiroFilterFactoryBean);
        configurationAdvice.initAfter(this.shiroPac4jConfigHolder);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisCache redisCache() {
        return this.redisCache;
    }

    @Bean("pac4jConfig")
    @ConditionalOnMissingBean
    public Config pac4jConfig() {
        return this.shiroPac4jConfigHolder.getConfig();
    }

    /**
     * session与token存储管理
     */
    @Bean
    @ConditionalOnMissingBean
    public SessionTokenStore sessionTokenStore() {
        return this.shiroPac4jConfigHolder.getSessionTokenStore();
    }

    @Bean("pac4jClients")
    @ConditionalOnMissingBean
    public Clients clients() {
        return this.shiroPac4jConfigHolder.getClients();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShiroRedisSessionDAO shiroRedisSessionDAO() {
        return this.shiroPac4jConfigHolder.getSessionDAO();
    }

    /**
     * shiro会话管理
     */
    @Bean
    @ConditionalOnMissingBean
    public SessionManager shiroSessionManage() {
        return this.shiroPac4jConfigHolder.getSessionManager();
    }

    /**
     * shiro securityManager
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityManager securityManager() {
        return this.shiroPac4jConfigHolder.getSecurityManager();
    }

    /**
     * 开启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证
     */
    @Bean
    @ConditionalOnProperty(value = {"shiro.isDebug"}, havingValue = "false")
    @ConditionalOnMissingBean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        return this.shiroPac4jConfigHolder.getAuthorizationAttributeSourceAdvisor();
    }

    /**
     * 开启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证
     */
    @Bean
    @ConditionalOnProperty(value = {"shiro.is-debug"}, havingValue = "false")
    @ConditionalOnMissingBean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor2(SecurityManager securityManager) {
        return this.shiroPac4jConfigHolder.getAuthorizationAttributeSourceAdvisor();
    }

    @Bean("shiroFilterMap")
    public Map<String, Filter> shiroFilterMap() {
        return this.shiroPac4jConfigHolder.getFilters();
    }

    /**
     * shiroFilterFactoryBean
     */
    @Bean(name = "shiroFilterFactory")
    @ConditionalOnMissingBean
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        return this.shiroPac4jConfigHolder.getShiroFilterFactoryBean();
    }

    @Bean("shiroFilterProxy")
    public FilterRegistrationBean<Filter> shiroFilterProxy() throws Exception {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter((Filter) this.shiroPac4jConfigHolder.getShiroFilterFactoryBean().getObject());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setName("shiroFilter");
        filterRegistrationBean.setOrder(Integer.MIN_VALUE);
        filterRegistrationBean.setEnabled(true);
        return filterRegistrationBean;
    }
}
