package org.linuxprobe.shiro.utils;

import io.buji.pac4j.subject.Pac4jPrincipal;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.pac4j.core.profile.CommonProfile;

public class SubjectUtils {
    /**
     * 获取当前会话对象的配置
     */
    public static CommonProfile getSessionCommonProfile() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            Pac4jPrincipal pac4jPrincipal = (Pac4jPrincipal) subject.getPrincipal();
            return pac4jPrincipal.getProfile();
        }
        return null;
    }

    /**
     * 获取当前会话对象
     */
    public static Subject getSessionSubject() {
        return SecurityUtils.getSubject();
    }
}
