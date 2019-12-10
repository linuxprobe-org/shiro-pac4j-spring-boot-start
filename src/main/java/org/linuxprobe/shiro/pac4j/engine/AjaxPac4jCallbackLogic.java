package org.linuxprobe.shiro.pac4j.engine;

import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.profile.CommonProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AjaxPac4jCallbackLogic<C extends CommonProfile, R> extends CallbackLogic<R, J2EContext> {
    default void onAjaxCallBack(HttpServletRequest request, HttpServletResponse response, C commonProfile) {

    }
}
