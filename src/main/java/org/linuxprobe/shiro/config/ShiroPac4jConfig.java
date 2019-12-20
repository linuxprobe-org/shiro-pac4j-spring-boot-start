package org.linuxprobe.shiro.config;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.pac4j.core.matching.Matcher;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class ShiroPac4jConfig {
    /**
     * url路径需要的过滤器
     */
    private LinkedHashMap<String, String> filterChainDefinitions;
    /**
     * 会话过期时间,单位秒
     */
    private long sessionTimeout = 60 * 60 * 2;
    /**
     * 用户授权信息缓存过期时间,单位秒
     */
    private long cacheTimeout = 120;
    /**
     * 是否启用调试模式，调试模型将关闭注解拦截
     */
    private Boolean isDebug = false;
    /**
     * 是否启用session
     */
    private Boolean enableSession = true;
    /**
     * 登陆地址
     */
    private String loginUrl;
    /**
     * Matchers 匹配器
     */
    private Map<String, Matcher> matchers;
    /**
     * 登陆后默认的首页
     */
    private String loginDefaultUrl = "/";

    /**
     * 登出后默认的首页
     */
    private String logoutDefaultUrl = "/";
    /**
     * 重定向配置,需要配置重定向拦截器（redirection）,否则不生效
     */
    private Map<String, String> redirections;
    /**
     * 自定义缓存前缀
     */
    private String cachePrefix;
}
