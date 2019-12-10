package org.linuxprobe.shiro.session;

import org.apache.shiro.session.Session;
import org.linuxprobe.luava.cache.impl.redis.RedisCache;
import org.linuxprobe.luava.shiro.redis.session.ShiroRedisSessionDAO;

public class Pac4jShiroRedisSessionDAO extends ShiroRedisSessionDAO {
    private SessionTokenStore sessionTokenStore;

    public Pac4jShiroRedisSessionDAO(RedisCache redisCache, SessionTokenStore sessionTokenStore) {
        super(redisCache);
        this.sessionTokenStore = sessionTokenStore;
    }

    @Override
    public void delete(Session session) {
        super.delete(session);
        this.sessionTokenStore.deleteMapBySessionId(session.getId().toString());
    }
}
