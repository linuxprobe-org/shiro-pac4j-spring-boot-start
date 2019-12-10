package org.linuxprobe.shiro.pac4j.engine;

import org.linuxprobe.luava.json.JacksonUtils;
import org.linuxprobe.shiro.data.Result;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.exception.HttpAction;

public interface AjaxPac4jSecurityLogic<R> extends SecurityLogic<R, J2EContext> {
    default HttpAction onAjaxUnauthorized(J2EContext context) {
        Result<Void> result = Result.fail(401, "Permission denied");
        return HttpAction.ok(context, JacksonUtils.toJsonString(result));
    }
}
