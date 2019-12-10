package org.linuxprobe.shiro.filter;


import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.linuxprobe.luava.servlet.HttpServletUtils;
import org.linuxprobe.shiro.data.Result;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthcFilter extends FormAuthenticationFilter {
    public static final String name = "authc";

    public void onAjaxAccessDenied(HttpServletRequest request, HttpServletResponse response) {
        HttpServletUtils.responseJson(request, response, Result.fail(401, "Permission denied"));
    }

    /**
     * 重写认证失败，当ajax请求时不重定向
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        if (HttpServletUtils.isAjax((HttpServletRequest) request)) {
            this.onAjaxAccessDenied((HttpServletRequest) request, (HttpServletResponse) response);
            return false;
        } else {
            return super.onAccessDenied(request, response);
        }
    }

    @Override
    public String getName() {
        return AuthcFilter.name;
    }
}
