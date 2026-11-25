package com.byahero.controller;

import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Handles /static/style.css and /static/board.js.
 * The browser asks for these files, so we read them from the static folder and send them.
 * (This is only for the frontend files. You do not need to edit this class.)
 */
public class StaticController extends BaseController {

    private String folder;

    public StaticController(String folder) {
        this.folder = folder;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (!path.startsWith("/static/")) {
            sendHtml(exchange, 404, "<p>File not found.</p>");
            return;
        }
        String fileName = path.substring("/static/".length());

        // safety: only plain file names, so nobody can open files outside the folder
        if (fileName.isEmpty() || fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            sendHtml(exchange, 404, "<p>File not found.</p>");
            return;
        }

        // only .css and .js files are allowed
        String contentType = null;
        if (fileName.endsWith(".css")) {
            contentType = "text/css; charset=UTF-8";
        } else if (fileName.endsWith(".js")) {
            contentType = "application/javascript; charset=UTF-8";
        }

        File file = new File(folder, fileName);
        if (contentType == null || !file.isFile()) {
            sendHtml(exchange, 404, "<p>File not found.</p>");
            return;
        }

        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        byte[] bytes = Files.readAllBytes(file.toPath());
        sendBytes(exchange, 200, contentType, bytes);
    }
}
