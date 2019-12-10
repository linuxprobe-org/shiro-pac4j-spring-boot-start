package org.linuxprobe.shiro.pac4j.engine;

import org.linuxprobe.luava.servlet.HttpServletUtils;
import org.linuxprobe.shiro.data.Result;
import org.linuxprobe.shiro.utils.SubjectUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.LogoutLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AjaxPac4jLogoutLogic<R> extends LogoutLogic<R, J2EContext> {
    default void onAjaxLogout(HttpServletRequest request, HttpServletResponse response) {
        HttpServletUtils.responseJson(request, response, Result.success());
        SubjectUtils.getSessionSubject().logout();
    }
}
