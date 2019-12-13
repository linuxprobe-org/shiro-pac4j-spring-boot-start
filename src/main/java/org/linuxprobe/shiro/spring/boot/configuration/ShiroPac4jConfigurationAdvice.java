package org.linuxprobe.shiro.spring.boot.configuration;

import org.apache.shiro.realm.Realm;
import org.linuxprobe.luava.servlet.HttpServletUtils;
import org.linuxprobe.shiro.data.Result;
import org.linuxprobe.shiro.filter.ShiroRootFilter;
import org.linuxprobe.shiro.pac4j.engine.DefaultPac4jCallbackLogic;
import org.linuxprobe.shiro.pac4j.engine.DefaultPac4jLogoutLogic;
import org.linuxprobe.shiro.pac4j.engine.DefaultPac4jSecurityLogic;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
        return new DefaultPac4jSecurityLogic<>(configHolder.getSessionTokenStore(), configHolder.getShiroProperties());
    }

    /**
     * 自定义shiro 拦截器异常处理
     */
    default ShiroRootFilter.ShiroFilterExceptionHandler getShiroFilterExceptionHandler(ShiroPac4jConfigHolder configHolder) {
        return new ShiroRootFilter.ShiroFilterExceptionHandler() {
            private final Logger logger = LoggerFactory.getLogger(ShiroPac4jConfigurationAdvice.class);

            @Override
            public void onException(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain, Exception exception) throws ServletException, IOException {
                if (this.logger.isErrorEnabled()) {
                    this.logger.error("", exception);
                }
                String message = exception.getMessage();
                if (message == null || message.isEmpty()) {
                    message = "服务器发生错误," + exception.getClass().getName();
                }
                HttpServletUtils.responseJson((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, Result.fail(message));
            }
        };
    }

    /**
     * 初始化后
     */
    default void initAfter(ShiroPac4jConfigHolder configHolder) {
    }
}
