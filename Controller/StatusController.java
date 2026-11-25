package com.byahero.controller;

import com.byahero.service.StatusService;
import com.byahero.util.FormParser;
import com.byahero.view.BoardPage;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/**
 * Handles POST /status - moves a bus to its next status.
 * Our own exceptions all extend RuntimeException, so
 * catching RuntimeException catches every error from the service.
 */
public class StatusController extends BaseController {

    private final StatusService statusService;
    private final BoardPage boardPage;

    public StatusController(StatusService statusService, BoardPage boardPage) {
        this.statusService = statusService;
        this.boardPage = boardPage;
    }

    // if someone opens /status directly in the browser, send them to the board
    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        redirect(exchange, "/board");
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        Map<String, String> form = readFormBody(exchange);
        String plate = FormParser.get(form, "plate");
        String nextStatus = FormParser.get(form, "nextStatus");

        try {
            statusService.updateStatus(plate, nextStatus);
            redirect(exchange, "/board");
        } catch (RuntimeException e) {
            // show the board again with a friendly error message
            sendHtml(exchange, 400, boardPage.render(e.getMessage()));
        }
    }
}
