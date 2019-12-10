package org.linuxprobe.shiro.filter;

import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Shiro跨域请求过滤：不再执行其它拦截器，直接返回OK;
 * 如果是跨域请求，则在response的Headers中设置安全控制信息，否则axios不能正确检测。
 */
public class ShiroOriginFilter extends AdviceFilter {
    public static final String name = "origin";

    /**
     * 如果返回 true 则继续拦截器链；否则中断后续的拦截器链的执行直接返回
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        String origin = httpRequest.getHeader("Origin");
        // 如果是跨域请求，在响应中设置安全Headers
        if (!"".equals(origin)) {
            String headers = httpRequest.getHeader("Access-Control-Request-Headers");
            httpResponse.setHeader("Access-control-Allow-Origin", origin);
            httpResponse.setHeader("Access-Control-Allow-Methods", "OPTIONS, POST, GET, PUT, PATCH, DELETE");
            httpResponse.setHeader("Access-Control-Allow-Headers",
                    "token, JSESSIONID" + (headers == null ? "" : ", " + headers));
        }
        // 如果是跨域的Options请求，不再执行其它拦截器，直接返回OK
        if (httpRequest.getMethod().equals("OPTIONS")) {
            httpResponse.setStatus(200);
            return false;
        }
        return super.preHandle(request, response);
    }

    @Override
    public String getName() {
        return ShiroOriginFilter.name;
    }
}
