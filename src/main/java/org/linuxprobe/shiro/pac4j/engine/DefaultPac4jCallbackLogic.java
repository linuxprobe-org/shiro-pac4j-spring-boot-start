package org.linuxprobe.shiro.pac4j.engine;

import io.buji.pac4j.engine.ShiroCallbackLogic;
import org.linuxprobe.luava.servlet.HttpServletUtils;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;

import java.util.Optional;

public class DefaultPac4jCallbackLogic<R> extends ShiroCallbackLogic<R, J2EContext> implements AjaxPac4jCallbackLogic<CommonProfile, R> {
    @Override
    public R perform(J2EContext context, Config config, HttpActionAdapter<R, J2EContext> httpActionAdapter, String inputDefaultUrl, Boolean inputSaveInSession, Boolean inputMultiProfile, Boolean inputRenewSession, String client) {
        R result = super.perform(context, config, httpActionAdapter, inputDefaultUrl, inputSaveInSession, inputMultiProfile, inputRenewSession, client);
        if (HttpServletUtils.isAjax(context.getRequest())) {
            Optional<CommonProfile> optional = this.getProfileManager(context, config).get(true);
            CommonProfile commonProfile = optional.orElse(null);
            if (commonProfile != null) {
                context.setResponseStatus(200);
                this.onAjaxCallBack(context.getRequest(), context.getResponse(), commonProfile);
            }
        }
        return result;
    }
}
