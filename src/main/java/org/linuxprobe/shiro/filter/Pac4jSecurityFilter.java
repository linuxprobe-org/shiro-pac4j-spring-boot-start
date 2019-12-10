package org.linuxprobe.shiro.filter;

import io.buji.pac4j.context.ShiroSessionStore;
import io.buji.pac4j.filter.SecurityFilter;
import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.http.adapter.J2ENopHttpActionAdapter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

@Getter
@Setter
public class Pac4jSecurityFilter extends SecurityFilter {
    public static final String name = "security";
    private boolean lazyVerify;

    public Pac4jSecurityFilter(SecurityLogic<Object, J2EContext> securityLogic) {
        this.setSecurityLogic(securityLogic);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Subject subject = SecurityUtils.getSubject();
        if (this.lazyVerify && subject.isAuthenticated()) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            assertNotNull("securityLogic", this.getSecurityLogic());
            assertNotNull("config", this.getConfig());
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            SessionStore<J2EContext> sessionStore = this.getConfig().getSessionStore();
            J2EContext context = new J2EContext(request, response, sessionStore != null ? sessionStore : ShiroSessionStore.INSTANCE);
            this.getSecurityLogic().perform(context, this.getConfig(), (ctx, profiles, parameters) -> {
                filterChain.doFilter(request, response);
                return null;
            }, J2ENopHttpActionAdapter.INSTANCE, this.getClients(), this.getAuthorizers(), this.getMatchers(), this.getMultiProfile());
        }
    }

    @Override
    public String getClients() {
        return this.getConfig().getClients().getClients().stream().map(Client::getName).map(str -> str + ",").reduce("", String::concat);
    }

    public String getName() {
        return Pac4jSecurityFilter.name;
    }
}
