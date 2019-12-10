package org.linuxprobe.shiro.pac4j.extractor;

import org.linuxprobe.luava.servlet.HttpServletUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;

public class HeaderParameterCookieExtrator implements CredentialsExtractor<TokenCredentials> {
    private String parameterName;
    private String headerName;
    private String cookieName;

    public HeaderParameterCookieExtrator(String headerName, String parameterName, String cookieName) {
        this.headerName = headerName;
        this.parameterName = parameterName;
        this.cookieName = cookieName;
    }

    @Override
    public TokenCredentials extract(WebContext context) {
        String value = context.getRequestParameter(this.parameterName);
        if (value == null) {
            value = context.getRequestHeader(this.headerName);
            if (value == null) {
                value = context.getRequestHeader(this.headerName.toLowerCase());
                if (value == null) {
                    value = HttpServletUtils.getCookieValue(((J2EContext) context).getRequest(), this.cookieName);
                }
            }
        }
        if (value == null) {
            return null;
        }
        return new TokenCredentials(value);
    }
}
