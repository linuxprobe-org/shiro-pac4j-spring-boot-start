package org.linuxprobe.shiro.pac4j.engine;

import org.linuxprobe.luava.servlet.HttpServletUtils;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;


public class DefaultPac4jLogoutLogic<R> extends DefaultLogoutLogic<R, J2EContext> implements AjaxPac4jLogoutLogic<R> {
    @Override
    public R perform(J2EContext context, Config config, HttpActionAdapter<R, J2EContext> httpActionAdapter, String defaultUrl, String inputLogoutUrlPattern, Boolean inputLocalLogout, Boolean inputDestroySession, Boolean inputCentralLogout) {
        if (HttpServletUtils.isAjax(context.getRequest())) {
            this.onAjaxLogout(context.getRequest(), context.getResponse());
            return null;
        } else {
            return super.perform(context, config, httpActionAdapter, defaultUrl, inputLogoutUrlPattern, inputLocalLogout, inputDestroySession, inputCentralLogout);
        }
    }
}