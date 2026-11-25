package com.byahero.controller;

import com.byahero.service.SessionService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/** Handles /logout. Only POST logs out, so another website cannot log you out with a link. */
public class LogoutController extends BaseController {

    private final SessionService sessions;

    public LogoutController(SessionService sessions) {
        this.sessions = sessions;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        redirect(exchange, "/board");
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        sessions.end(SessionCookie.read(exchange));
        SessionCookie.clear(exchange);
        redirect(exchange, "/login");
    }
}
