package com.byahero.controller;

import com.byahero.util.FormParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * INHERITANCE: the parent of all controllers.
 * It holds the code every controller needs, so the children do not repeat it.
 *
 * How it works: the web server calls handle() for every request.
 * handle() looks at the request type and calls handleGet() or handlePost().
 * Each child controller overrides the one(s) it needs.
 */
public abstract class BaseController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            if (method.equalsIgnoreCase("GET")) {
                handleGet(exchange);
            } else if (method.equalsIgnoreCase("POST")) {
                handlePost(exchange);
            } else {
                sendHtml(exchange, 405, "<p>Method not allowed.</p>");
            }
        } catch (Exception e) {
            // Something unexpected happened. Show a friendly message, never a stack trace.
            System.err.println("Server error: " + e.getMessage());
            try {
                sendHtml(exchange, 500, "<p>Something went wrong. Please go back and try again.</p>");
            } catch (Exception ignored) {
                // the response was already started, nothing more we can do
            }
        } finally {
            exchange.close();
        }
    }

    /** Children override this to answer GET requests (opening a page). */
    protected void handleGet(HttpExchange exchange) throws IOException {
        sendHtml(exchange, 405, "<p>Method not allowed.</p>");
    }

    /** Children override this to answer POST requests (submitting a form). */
    protected void handlePost(HttpExchange exchange) throws IOException {
        sendHtml(exchange, 405, "<p>Method not allowed.</p>");
    }

    /** Reads the submitted form and returns its fields as a Map (name -> value). */
    protected Map<String, String> readFormBody(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readAllBytes();
        String body = new String(bytes, StandardCharsets.UTF_8);
        return FormParser.parse(body);
    }

    /** Reads the part of the address after "?", like /route?destination=Manila */
    protected Map<String, String> readQuery(HttpExchange exchange) {
        String query = exchange.getRequestURI().getRawQuery();
        return FormParser.parse(query);
    }

    /** Sends an HTML page back to the browser. */
    protected void sendHtml(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        sendBytes(exchange, statusCode, "text/html; charset=UTF-8", bytes);
    }

    /** Sends any kind of content (html, css, js) back to the browser. */
    protected void sendBytes(HttpExchange exchange, int statusCode, String contentType, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream out = exchange.getResponseBody();
        out.write(bytes);
        out.close();
    }

    /** Tells the browser to go to another page. */
    protected void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
    }
}
