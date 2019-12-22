package org.linuxprobe.shiro.session;

import io.buji.pac4j.context.ShiroSessionStore;
import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.client.finder.ClientFinder;
import org.pac4j.core.client.finder.DefaultSecurityClientFinder;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.credentials.TokenCredentials;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class Pac4jWebSessionManager extends DefaultWebSessionManager {
    private Config config;
    private SessionTokenStore sessionTokenStore;
    private ClientFinder clientFinder = new DefaultSecurityClientFinder();

    public Pac4jWebSessionManager(SessionTokenStore sessionTokenStore, Config config) {
        this.sessionTokenStore = sessionTokenStore;
        this.config = config;
    }

    private String getClients() {
        return this.config.getClients().getClients().stream().map(Client::getName).map(str -> str + ",").reduce("", String::concat);
    }

    private String getToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            J2EContext context = new J2EContext(request, response, ShiroSessionStore.INSTANCE);
            List<Client> currentClients = this.clientFinder.find(this.config.getClients(), context, this.getClients());
            if (currentClients != null && !currentClients.isEmpty()) {
                for (Client currentClient : currentClients) {
                    if (currentClient instanceof DirectClient) {
                        TokenCredentials credentials = (TokenCredentials) currentClient.getCredentials(context);
                        if (credentials != null) {
                            return credentials.getToken();
                        }
                    }
                }
            }
            return null;
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        Serializable sessionId = null;
        String token = this.getToken((HttpServletRequest) request, (HttpServletResponse) response);
        if (token != null) {
            sessionId = this.sessionTokenStore.getSessionIdByToken(token);
        }
        if (sessionId == null) {
            sessionId = super.getSessionId(request, response);
        }
        return sessionId;
    }
}
