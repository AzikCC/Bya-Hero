package com.byahero.controller;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/** Handles "/": sends the visitor to the board. Any other unknown address shows "not found". */
public class RootController extends BaseController {

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            redirect(exchange, "/board");
        } else {
            sendHtml(exchange, 404, "<p>Page not found. <a href=\"/board\">Back to board</a></p>");
        }
    }
}
