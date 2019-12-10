package org.linuxprobe.shiro.session;

import java.util.concurrent.TimeUnit;

public interface SessionTokenStore {
    /**
     * 根据token获取sessionId
     */
    String getSessionIdByToken(String token);

    /**
     * 增加映射
     */
    void addMap(String token, String sessionId, long timeout, TimeUnit timeUnit);

    /**
     * 根据sessionId删除映射
     */
    void deleteMapBySessionId(String sessionId);
}
