package org.linuxprobe.shiro.spring.boot.configuration;

import org.apache.shiro.realm.Realm;
import org.linuxprobe.shiro.base.pac4j.engine.DefaultPac4jCallbackLogic;
import org.linuxprobe.shiro.base.pac4j.engine.DefaultPac4jLogoutLogic;
import org.linuxprobe.shiro.base.pac4j.engine.DefaultPac4jSecurityLogic;
import org.linuxprobe.shiro.base.pac4j.jwt.Pac4jJwtAuthenticator;
import org.linuxprobe.shiro.base.pac4j.jwt.Pac4jJwtGenerator;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface ShiroPac4jConfigurationAdvice {
    /**
     * 初始化前
     */
    default void initBefore(ShiroPac4jProperties shiroPac4jProperties) {
    }

    /**
     * 自定义jwt生成
     *
     * @param configHolder shiro pac4j配置
     */
    default <U extends CommonProfile> Pac4jJwtGenerator<U> getJwtGenerator(ShiroPac4jConfigHolder configHolder) {
        return new Pac4jJwtGenerator<U>(configHolder.getSignatureConfiguration(), configHolder.getEncryptionConfiguration(), configHolder.getSessionTokenStore());
    }

    /**
     * 自定义jwt校验
     *
     * @param configHolder shiro pac4j配置
     */
    default JwtAuthenticator getJwtAuthenticator(ShiroPac4jConfigHolder configHolder) {
        Pac4jJwtAuthenticator jwtAuthenticator = new Pac4jJwtAuthenticator();
        jwtAuthenticator.setSignatureConfiguration(configHolder.getSignatureConfiguration());
        jwtAuthenticator.setEncryptionConfiguration(configHolder.getEncryptionConfiguration());
        jwtAuthenticator.setSessionTokenStore(configHolder.getSessionTokenStore());
        return jwtAuthenticator;
    }

    /**
     * 自定义realm
     */
    default List<Realm> getRealms(ShiroPac4jConfigHolder configHolder) {
        return new LinkedList<>();
    }

    /**
     * 自定义client
     *
     * @param configHolder shiro pac4j配置
     */
    default List<Client> getClients(ShiroPac4jConfigHolder configHolder) {
        return new LinkedList<>();
    }

    /**
     * 自定义拦截器
     *
     * @param configHolder shiro pac4j配置
     */
    default Map<String, Filter> getFilter(ShiroPac4jConfigHolder configHolder) {
        return new HashMap<>();
    }

    /**
     * 自定义登出处理
     *
     * @param configHolder shiro pac4j配置
     */
    default LogoutLogic<Object, J2EContext> getLogoutLogic(ShiroPac4jConfigHolder configHolder) {
        return new DefaultPac4jLogoutLogic<>();
    }


    /**
     * 自定义oauth2回调
     *
     * @param configHolder shiro pac4j配置
     */
    default CallbackLogic<Object, J2EContext> getCallbackLogic(ShiroPac4jConfigHolder configHolder) {
        return new DefaultPac4jCallbackLogic<>();
    }

    /**
     * 自定义安全认证
     *
     * @param configHolder shiro pac4j配置
     */
    default SecurityLogic<Object, J2EContext> getSecurityLogic(ShiroPac4jConfigHolder configHolder) {
        return new DefaultPac4jSecurityLogic<>(configHolder.getSessionTokenStore());
    }

    /**
     * 初始化后
     */
    default void initAfter(ShiroPac4jConfigHolder configHolder) {
    }
}
