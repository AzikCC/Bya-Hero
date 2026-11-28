package com.byahero.service.impl;

import com.byahero.service.SessionService;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps sessions in memory. A session lasts 2 hours since the last page visit.
 * Restarting the program logs everybody out (that is fine for this app).
 */
public class InMemorySessionService implements SessionService {

    private static final long TIMEOUT_MILLIS = 2 * 60 * 60 * 1000L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static class Session {
        final String username;
        long lastSeen;

        Session(String username) {
            this.username = username;
            this.lastSeen = System.currentTimeMillis();
        }
    }

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    @Override
    public String start(String username) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new Session(username));
        return token;
    }

    @Override
    public String getUsername(String token) {
        if (token == null) {
            return null;
        }
        Session session = sessions.get(token);
        if (session == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        if (now - session.lastSeen > TIMEOUT_MILLIS) {
            sessions.remove(token);
            return null;
        }
        session.lastSeen = now;
        return session.username;
    }

    @Override
    public void end(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
