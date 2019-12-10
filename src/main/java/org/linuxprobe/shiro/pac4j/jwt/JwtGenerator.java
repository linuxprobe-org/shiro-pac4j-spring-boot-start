package org.linuxprobe.shiro.pac4j.jwt;

import org.pac4j.core.profile.CommonProfile;

public interface JwtGenerator<U extends CommonProfile> {
    /**
     * 生成jwt字符串
     *
     * @param expiresIn 有效时长,单位秒
     * @param profile   生成jwt的信息配置
     */
    String generate(long expiresIn, U profile);
}
