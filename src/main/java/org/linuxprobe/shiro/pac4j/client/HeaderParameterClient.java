package org.linuxprobe.shiro.pac4j.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.linuxprobe.shiro.pac4j.extractor.HeaderParameterExtrator;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.creator.ProfileCreator;
import org.pac4j.core.util.CommonHelper;

@NoArgsConstructor
@Getter
@Setter
public class HeaderParameterClient extends DirectClient<TokenCredentials, CommonProfile> {
    private String parameterName = "";
    private String headerName = "";

    public HeaderParameterClient(String headerName, String parameterName, Authenticator tokenAuthenticator) {
        this.headerName = headerName;
        this.parameterName = parameterName;
        this.defaultAuthenticator(tokenAuthenticator);
    }

    public HeaderParameterClient(String headerName, String parameterName, Authenticator tokenAuthenticator,
                                 ProfileCreator profileCreator) {
        this.headerName = headerName;
        this.parameterName = parameterName;
        this.defaultAuthenticator(tokenAuthenticator);
        this.defaultProfileCreator(profileCreator);
    }

    @Override
    protected void clientInit() {
        CommonHelper.assertNotBlank("parameterName", this.parameterName);
        CommonHelper.assertNotBlank("headerName", this.headerName);
        this.defaultCredentialsExtractor(new HeaderParameterExtrator(this.headerName, this.parameterName));
    }
}
