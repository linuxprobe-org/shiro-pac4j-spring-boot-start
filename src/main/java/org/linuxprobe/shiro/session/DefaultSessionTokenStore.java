package org.linuxprobe.shiro.session;

import org.apache.commons.codec.digest.DigestUtils;
import org.linuxprobe.luava.cache.impl.redis.RedisCache;

import java.util.concurrent.TimeUnit;

public class DefaultSessionTokenStore implements SessionTokenStore {
    private static final String tokenMapSessionIdPrefix = "shiro:tokenMapSessionId:";
    private static final String sessionIdMapTokenPrefix = "shiro:sessionIdMapToken:";
    private RedisCache redisCache;

    public DefaultSessionTokenStore(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    @Override
    public String getSessionIdByToken(String token) {
        return this.redisCache.get(DefaultSessionTokenStore.tokenMapSessionIdPrefix + this.tokenToMd5(token));
    }

    @Override
    public void addMap(String token, String sessionId, long timeout, TimeUnit timeUnit) {
        String tokenMd5 = this.tokenToMd5(token);
        this.redisCache.set(DefaultSessionTokenStore.tokenMapSessionIdPrefix + tokenMd5, sessionId, timeout, timeUnit);
        this.redisCache.set(DefaultSessionTokenStore.sessionIdMapTokenPrefix + sessionId, token, timeout, timeUnit);
    }


    @Override
    public void deleteMapBySessionId(String sessionId) {
        String token = this.redisCache.get(DefaultSessionTokenStore.sessionIdMapTokenPrefix + sessionId);
        if (token != null) {
            String tokenMd5 = this.tokenToMd5(token);
            this.redisCache.delete(DefaultSessionTokenStore.sessionIdMapTokenPrefix + sessionId);
            this.redisCache.delete(DefaultSessionTokenStore.tokenMapSessionIdPrefix + tokenMd5);
        }
    }

    private String tokenToMd5(String token) {
        return DigestUtils.md5Hex(token);
    }
}
