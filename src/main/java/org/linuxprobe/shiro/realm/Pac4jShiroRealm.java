package org.linuxprobe.shiro.realm;

import io.buji.pac4j.realm.Pac4jRealm;
import io.buji.pac4j.subject.Pac4jPrincipal;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.linuxprobe.shiro.pac4j.authc.ClientAuthorizationInfo;
import org.linuxprobe.shiro.utils.ShiroCacheKeyCompute;
import org.pac4j.core.profile.CommonProfile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pac4jShiroRealm extends Pac4jRealm {
    private String cachePrefix;
    private ClientAuthorizationInfo clientAuthorizationInfo;

    public Pac4jShiroRealm(ClientAuthorizationInfo clientAuthorizationInfo) {
        this.clientAuthorizationInfo = clientAuthorizationInfo;
    }

    public Pac4jShiroRealm(ClientAuthorizationInfo clientAuthorizationInfo, String cachePrefix) {
        this.clientAuthorizationInfo = clientAuthorizationInfo;
        this.cachePrefix = cachePrefix;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        Pac4jPrincipal principal = principals.oneByType(Pac4jPrincipal.class);
        if (principal != null) {
            List<CommonProfile> profiles = principal.getProfiles();
            for (CommonProfile profile : profiles) {
                if (profile != null) {
                    roles.addAll(this.clientAuthorizationInfo.getRoles(profile));
                    permissions.addAll(this.clientAuthorizationInfo.getPermissions(profile));
                }
            }
        }
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRoles(roles);
        simpleAuthorizationInfo.addStringPermissions(permissions);
        return simpleAuthorizationInfo;
    }

    /**
     * 获取授权缓存信息的key
     */
    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        Pac4jPrincipal pac4jPrincipal = (Pac4jPrincipal) principals.getPrimaryPrincipal();
        CommonProfile profile = pac4jPrincipal.getProfile();
        return ShiroCacheKeyCompute.compute(profile, this.cachePrefix);
    }
}
