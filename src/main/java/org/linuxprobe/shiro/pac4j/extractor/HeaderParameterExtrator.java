package org.linuxprobe.shiro.pac4j.extractor;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;

public class HeaderParameterExtrator implements CredentialsExtractor<TokenCredentials> {
    private String parameterName;
    private String headerName;

    public HeaderParameterExtrator(String headerName, String parameterName) {
        this.headerName = headerName;
        this.parameterName = parameterName;
    }

    @Override
    public TokenCredentials extract(WebContext context) {
        String value = context.getRequestParameter(this.parameterName);
        if (value == null) {
            value = context.getRequestHeader(this.headerName);
            if (value == null) {
                value = context.getRequestHeader(this.headerName.toLowerCase());
            }
        }
        if (value == null) {
            return null;
        }
        return new TokenCredentials(value);
    }
}
