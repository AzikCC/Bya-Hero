package com.byahero.controller;

import com.byahero.service.SessionService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * A "gate" in front of another controller. If the visitor is not logged in they are
 * sent to /login; if they are, the request goes on to the real controller.
 *
 * OPEN/CLOSED: AdminController is not edited at all. To protect another page later,
 * wrap it in AppConfig:  new AuthGuard(sessions, new SomeController(...))
 */
public class AuthGuard implements HttpHandler {

    private final SessionService sessions;
    private final HttpHandler next;

    public AuthGuard(SessionService sessions, HttpHandler next) {
        this.sessions = sessions;
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String username = sessions.getUsername(SessionCookie.read(exchange));
        if (username == null) {
            // drain the body so the connection stays healthy, then send them to the login page
            exchange.getRequestBody().readAllBytes();
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(303, -1);
            exchange.close();
            return;
        }
        next.handle(exchange);
    }
}
