package org.linuxprobe.shiro.utils;

import org.pac4j.core.profile.CommonProfile;

/**
 * shiro缓存key计算
 */
public class ShiroCacheKeyCompute {
    public static String compute(CommonProfile profile, String cachePrefix) {
        String key = profile.getClientName() + ":" + profile.getId();
        if (cachePrefix != null && !cachePrefix.isEmpty()) {
            key = cachePrefix + ":" + key;
        }
        return key;
    }
}
