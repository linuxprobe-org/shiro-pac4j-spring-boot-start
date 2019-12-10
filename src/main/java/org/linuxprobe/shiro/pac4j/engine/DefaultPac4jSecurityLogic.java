package org.linuxprobe.shiro.pac4j.engine;

import io.buji.pac4j.engine.ShiroSecurityLogic;
import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.linuxprobe.luava.servlet.HttpServletUtils;
import org.linuxprobe.shiro.config.ShiroPac4jConfig;
import org.linuxprobe.shiro.session.SessionTokenStore;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.engine.SecurityGrantedAccessAdapter;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.pac4j.core.util.CommonHelper.*;

@Getter
@Setter
public class DefaultPac4jSecurityLogic<R> extends ShiroSecurityLogic<R, J2EContext> implements AjaxPac4jSecurityLogic<R> {
    private SessionTokenStore sessionTokenStore;
    private ShiroPac4jConfig shiroPac4jConfig;

    public DefaultPac4jSecurityLogic(SessionTokenStore sessionTokenStore, ShiroPac4jConfig shiroPac4jConfig) {
        this.sessionTokenStore = sessionTokenStore;
        assertNotNull("shiroPac4jConfig", shiroPac4jConfig);
        this.shiroPac4jConfig = shiroPac4jConfig;
    }

    @Override
    protected HttpAction unauthorized(J2EContext context, List<Client> currentClients) {
        if (HttpServletUtils.isAjax(context.getRequest())) {
            return this.onAjaxUnauthorized(context);
        } else {
            return super.unauthorized(context, currentClients);
        }
    }

    private void addTokenMapSession(Credentials credentials) {
        if (credentials instanceof TokenCredentials && this.shiroPac4jConfig.getEnableSession() && this.sessionTokenStore != null) {
            String token = ((TokenCredentials) credentials).getToken();
            Session session = SecurityUtils.getSubject().getSession();
            this.sessionTokenStore.addMap(token, session.getId().toString(), session.getTimeout(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public R perform(J2EContext context, Config config, SecurityGrantedAccessAdapter<R, J2EContext> securityGrantedAccessAdapter, HttpActionAdapter<R, J2EContext> httpActionAdapter, String clients, String authorizers, String matchers, Boolean inputMultiProfile, Object... parameters) {
        this.logger.debug("=== SECURITY ===");
        HttpAction action;
        try {
            // default value
            final boolean multiProfile;
            if (inputMultiProfile == null) {
                multiProfile = false;
            } else {
                multiProfile = inputMultiProfile;
            }
            // checks
            assertNotNull("context", context);
            assertNotNull("config", config);
            assertNotNull("httpActionAdapter", httpActionAdapter);
            assertNotNull("clientFinder", this.getClientFinder());
            assertNotNull("authorizationChecker", this.getAuthorizationChecker());
            assertNotNull("matchingChecker", this.getMatchingChecker());
            assertNotNull("profileStorageDecision", this.getProfileStorageDecision());
            final Clients configClients = config.getClients();
            assertNotNull("configClients", configClients);
            // logic
            this.logger.debug("url: {}", context.getFullRequestURL());
            this.logger.debug("matchers: {}", matchers);
            if (this.getMatchingChecker().matches(context, matchers, config.getMatchers())) {
                this.logger.debug("clients: {}", clients);
                final List<Client> currentClients = this.getClientFinder().find(configClients, context, clients);
                this.logger.debug("currentClients: {}", currentClients);
                final boolean loadProfilesFromSession = this.getProfileStorageDecision().mustLoadProfilesFromSession(context, currentClients);
                this.logger.debug("loadProfilesFromSession: {}", loadProfilesFromSession);
                final ProfileManager manager = this.getProfileManager(context, config);
                List<CommonProfile> profiles = manager.getAll(loadProfilesFromSession);
                this.logger.debug("profiles: {}", profiles);
                // no profile and some current clients
                if (isEmpty(profiles) && isNotEmpty(currentClients)) {
                    boolean updated = false;
                    // loop on all clients searching direct ones to perform authentication
                    for (final Client currentClient : currentClients) {
                        if (currentClient instanceof DirectClient) {
                            this.logger.debug("Performing authentication for direct client: {}", currentClient);
                            final Credentials credentials = currentClient.getCredentials(context);
                            //自定义实现
                            this.addTokenMapSession(credentials);
                            this.logger.debug("credentials: {}", credentials);
                            final CommonProfile profile = currentClient.getUserProfile(credentials, context);
                            this.logger.debug("profile: {}", profile);
                            if (profile != null) {
                                final boolean saveProfileInSession = this.getProfileStorageDecision().mustSaveProfileInSession(context,
                                        currentClients, (DirectClient) currentClient, profile);
                                this.logger.debug("saveProfileInSession: {} / multiProfile: {}", saveProfileInSession, multiProfile);
                                manager.save(saveProfileInSession, profile, multiProfile);
                                updated = true;
                                if (!multiProfile) {
                                    break;
                                }
                            }
                        }
                    }
                    if (updated) {
                        profiles = manager.getAll(loadProfilesFromSession);
                        this.logger.debug("new profiles: {}", profiles);
                    }
                }
                // we have profile(s) -> check authorizations
                if (isNotEmpty(profiles)) {
                    this.logger.debug("authorizers: {}", authorizers);
                    if (this.getAuthorizationChecker().isAuthorized(context, profiles, authorizers, config.getAuthorizers())) {
                        this.logger.debug("authenticated and authorized -> grant access");
                        return securityGrantedAccessAdapter.adapt(context, profiles, parameters);
                    } else {
                        this.logger.debug("forbidden");
                        action = this.forbidden(context, currentClients, profiles, authorizers);
                    }
                } else {
                    if (this.startAuthentication(context, currentClients)) {
                        this.logger.debug("Starting authentication");
                        this.saveRequestedUrl(context, currentClients);
                        action = this.redirectToIdentityProvider(context, currentClients);
                    } else {
                        this.logger.debug("unauthorized");
                        action = this.unauthorized(context, currentClients);
                    }
                }

            } else {
                this.logger.debug("no matching for this request -> grant access");
                return securityGrantedAccessAdapter.adapt(context, Arrays.asList(), parameters);
            }

        } catch (final Exception e) {
            return this.handleException(e, httpActionAdapter, context);
        }

        return httpActionAdapter.adapt(action.getCode(), context);
    }
}
