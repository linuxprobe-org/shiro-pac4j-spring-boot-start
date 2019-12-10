package org.linuxprobe.shiro.pac4j.authc;

import org.pac4j.core.profile.CommonProfile;

import java.util.Set;

public interface ClientAuthorizationInfo {
    Set<String> getRoles(CommonProfile profile);

    Set<String> getPermissions(CommonProfile profile);
}
