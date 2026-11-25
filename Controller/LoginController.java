package com.byahero.controller;

import com.byahero.service.AuthService;
import com.byahero.service.SessionService;
import com.byahero.util.FormParser;
import com.byahero.view.HtmlUtil;
import com.byahero.view.TemplateLoader;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles /login.
 *   GET  = show the login form (or go to /admin if already logged in)
 *   POST = check the username and password, start a session, go to /admin
 */
public class LoginController extends BaseController {

    private final AuthService auth;
    private final SessionService sessions;
    private final TemplateLoader templates;

    public LoginController(AuthService auth, SessionService sessions, TemplateLoader templates) {
        this.auth = auth;
        this.sessions = sessions;
        this.templates = templates;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        if (sessions.getUsername(SessionCookie.read(exchange)) != null) {
            redirect(exchange, "/admin");
            return;
        }
        sendHtml(exchange, 200, render("", ""));
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        Map<String, String> form = readFormBody(exchange);
        String username = FormParser.get(form, "username");
        String password = form.get("password"); // not trimmed: spaces can be part of a password

        try {
            String realName = auth.login(username, password);
            // always a NEW token at login, and the old one (if any) is ended
            sessions.end(SessionCookie.read(exchange));
            SessionCookie.write(exchange, sessions.start(realName));
            redirect(exchange, "/admin");
        } catch (RuntimeException e) {
            sendHtml(exchange, 401, render(e.getMessage(), username));
        }
    }

    private String render(String errorMessage, String username) {
        Map<String, String> values = new HashMap<String, String>();
        values.put("errorMessage", HtmlUtil.escape(errorMessage));
        values.put("username", HtmlUtil.escape(username));
        return templates.render("login.html", values);
    }
}
