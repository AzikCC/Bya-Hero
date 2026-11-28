package com.byahero.service;

/** Remembers who is logged in. The browser only holds a random token, never the username. */
public interface SessionService {

    /** Starts a session and returns its token. */
    String start(String username);

    /** Returns the username of a valid token, or null if the token is unknown or expired. */
    String getUsername(String token);

    /** Ends a session (logout). Unknown tokens are ignored. */
    void end(String token);
}
