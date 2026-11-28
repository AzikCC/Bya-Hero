package com.byahero.controller;

import com.sun.net.httpserver.HttpExchange;

import java.util.List;

/** Reads and writes the login cookie. The cookie holds only a random token. */
public class SessionCookie {

    public static final String NAME = "BYAHERO_SESSION";
    private static final int MAX_AGE_SECONDS = 2 * 60 * 60;

    /** Returns the token the browser sent, or null if there is none. */
    public static String read(HttpExchange exchange) {
        List<String> headers = exchange.getRequestHeaders().get("Cookie");
        if (headers == null) {
            return null;
        }
        for (int i = 0; i < headers.size(); i++) {
            String[] parts = headers.get(i).split(";");
            for (int j = 0; j < parts.length; j++) {
                String part = parts[j].trim();
                if (part.startsWith(NAME + "=")) {
                    return part.substring(NAME.length() + 1);
                }
            }
        }
        return null;
    }

    /**
     * HttpOnly = JavaScript cannot read it. SameSite=Lax = other websites cannot
     * make the browser send it with a form post (this also blocks CSRF attacks).
     */
    public static void write(HttpExchange exchange, String token) {
        exchange.getResponseHeaders().add("Set-Cookie",
                NAME + "=" + token + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=" + MAX_AGE_SECONDS);
    }

    /** Tells the browser to forget the cookie. */
    public static void clear(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Set-Cookie",
                NAME + "=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
    }
}
