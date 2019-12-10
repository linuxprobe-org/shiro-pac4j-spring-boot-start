package org.linuxprobe.shiro.filter;

import org.apache.shiro.web.servlet.AdviceFilter;
import org.linuxprobe.shiro.config.ShiroPac4jConfig;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class RedirectionFilter extends AdviceFilter {
    public static final String name = "redirection";
    private ShiroPac4jConfig shiroProperties;

    public RedirectionFilter(ShiroPac4jConfig shiroProperties) {
        this.shiroProperties = shiroProperties;
    }

    /**
     * 如果返回 true 则继续拦截器链；否则中断后续的拦截器链的执行直接返回
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        Map<String, String> redirections = this.shiroProperties.getRedirections();
        if (redirections != null && !redirections.isEmpty()) {
            String requestUri = httpServletRequest.getRequestURI();
            if (redirections.containsKey(requestUri)) {
                String redirectUrl = redirections.get(requestUri);
                String queryString = httpServletRequest.getQueryString();
                if (queryString != null) {
                    if (redirectUrl.contains("?")) {
                        redirectUrl += "&" + queryString;
                    } else {
                        redirectUrl += "?" + queryString;
                    }
                }
                if (redirectUrl.toLowerCase().startsWith("http")) {
                    httpServletResponse.sendRedirect(redirectUrl);
                } else {
                    httpServletResponse.setStatus(302);
                    httpServletResponse.setHeader("Location", redirectUrl);
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return RedirectionFilter.name;
    }
}
