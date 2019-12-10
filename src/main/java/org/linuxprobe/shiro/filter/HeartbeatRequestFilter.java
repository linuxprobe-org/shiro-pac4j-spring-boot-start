package org.linuxprobe.shiro.filter;

import org.apache.shiro.web.servlet.AdviceFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 心跳包拦截器
 */
public class HeartbeatRequestFilter extends AdviceFilter {
    public static final String name = "heartbeat";

    /**
     * 如果返回 true 则继续拦截器链；否则中断后续的拦截器链的执行直接返回
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        return !httpServletRequest.getMethod().equalsIgnoreCase("head");
    }

    @Override
    public String getName() {
        return HeartbeatRequestFilter.name;
    }
}
