package com.byahero.controller;

import com.byahero.service.BusService;
import com.byahero.util.FormParser;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/**
 * Handles POST /status - moves a bus to its next status.
 * Our own exceptions all extend RuntimeException, so
 * catching RuntimeException catches every error from the service.
 */
public class StatusController extends BaseController {

    private BusService busService;
    private BoardController boardController;

    public StatusController(BusService busService, BoardController boardController) {
        this.busService = busService;
        this.boardController = boardController;
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
            busService.updateStatus(plate, nextStatus);
            redirect(exchange, "/board");
        } catch (RuntimeException e) {
            // show the board again with a friendly error message
            String page = boardController.render(e.getMessage());
            sendHtml(exchange, 400, page);
        }
    }
}
