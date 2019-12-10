package org.linuxprobe.shiro.pac4j.client;

import org.linuxprobe.shiro.pac4j.extractor.HeaderParameterCookieExtrator;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.creator.ProfileCreator;
import org.pac4j.core.util.CommonHelper;

public class HeaderParameterCookieClient extends DirectClient<TokenCredentials, CommonProfile> {
    private String parameterName = "";
    private String headerName = "";
    private String cookieName = "";

    public HeaderParameterCookieClient(String headerName, String parameterName, String cookieName, Authenticator tokenAuthenticator) {
        this.headerName = headerName;
        this.parameterName = parameterName;
        this.cookieName = cookieName;
        this.defaultAuthenticator(tokenAuthenticator);
    }

    public HeaderParameterCookieClient(String headerName, String parameterName, String cookieName, Authenticator tokenAuthenticator,
                                       ProfileCreator profileCreator) {
        this.headerName = headerName;
        this.parameterName = parameterName;
        this.cookieName = cookieName;
        this.defaultAuthenticator(tokenAuthenticator);
        this.defaultProfileCreator(profileCreator);
    }

    @Override
    protected void clientInit() {
        CommonHelper.assertNotBlank("parameterName", this.parameterName);
        CommonHelper.assertNotBlank("headerName", this.headerName);
        CommonHelper.assertNotBlank("cookieName", this.cookieName);
        this.defaultCredentialsExtractor(new HeaderParameterCookieExtrator(this.headerName, this.parameterName, this.cookieName));
    }
}
