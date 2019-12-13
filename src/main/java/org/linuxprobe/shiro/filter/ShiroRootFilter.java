package org.linuxprobe.shiro.filter;

import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.concurrent.Callable;

public class ShiroRootFilter extends AbstractShiroFilter {
    @Getter
    @Setter
    private ShiroFilterExceptionHandler exceptionHandler;

    public ShiroRootFilter(WebSecurityManager webSecurityManager, FilterChainResolver resolver) {
        super();
        if (webSecurityManager == null) {
            throw new IllegalArgumentException("WebSecurityManager property cannot be null.");
        }
        this.setSecurityManager(webSecurityManager);
        if (resolver != null) {
            this.setFilterChainResolver(resolver);
        }
    }

    @Override
    protected void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws ServletException, IOException {
        try {
            final ServletRequest request = this.prepareServletRequest(servletRequest, servletResponse, chain);
            final ServletResponse response = this.prepareServletResponse(request, servletResponse, chain);
            final Subject subject = this.createSubject(request, response);
            //noinspection unchecked
            subject.execute(new Callable() {
                @Override
                public Object call() throws Exception {
                    ShiroRootFilter.this.updateSessionLastAccessTime(request, response);
                    ShiroRootFilter.this.executeChain(request, response, chain);
                    return null;
                }
            });
        } catch (Exception ex) {
            if (this.exceptionHandler != null) {
                this.exceptionHandler.onException(servletRequest, servletResponse, chain, ex);
            } else {
                throw new ServletException(ex.getMessage(), ex);
            }
        }
    }

    /**
     * shiro 拦截器异常处理
     */
    @FunctionalInterface
    public static interface ShiroFilterExceptionHandler {
        void onException(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain, Exception exception) throws ServletException, IOException;
    }
}
