package com.byahero.controller;

import com.byahero.view.BoardPage;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/** Handles GET /board - the public board. All the HTML building is done by BoardPage. */
public class BoardController extends BaseController {

    private final BoardPage boardPage;

    public BoardController(BoardPage boardPage) {
        this.boardPage = boardPage;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        sendHtml(exchange, 200, boardPage.render(""));
    }
}
