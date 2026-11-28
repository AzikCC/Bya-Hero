package com.byahero.controller;

import com.byahero.controller.admin.AdminSection;
import com.byahero.util.FormParser;
import com.byahero.view.HtmlUtil;
import com.byahero.view.TemplateLoader;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles /admin.
 *   GET  = show the tables and forms
 *   POST = run an add / update / delete action
 *
 * Each form on the page sends two special values:
 *   entity = "fare", "route" or "bus"   (which AdminSection should handle it)
 *   action = "add", "update" or "delete" (what to do with it)
 *
 * This class only routes the request to the right AdminSection and shows the page.
 * What each section does lives in package controller.admin.
 */
public class AdminController extends BaseController {

    private final List<AdminSection> sections;
    private final TemplateLoader templates;

    public AdminController(List<AdminSection> sections, TemplateLoader templates) {
        this.sections = sections;
        this.templates = templates;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        sendHtml(exchange, 200, render("", ""));
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        Map<String, String> form = readFormBody(exchange);
        String entity = FormParser.get(form, "entity");
        String action = FormParser.get(form, "action");

        try {
            AdminSection section = findSection(entity);
            String message = section.handle(action, form);
            sendHtml(exchange, 200, render("", message));
        } catch (RuntimeException e) {
            sendHtml(exchange, 400, render(e.getMessage(), ""));
        }
    }

    private AdminSection findSection(String entity) {
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).getName().equals(entity)) {
                return sections.get(i);
            }
        }
        throw new IllegalArgumentException("Unknown admin form.");
    }

    private String render(String errorMessage, String successMessage) {
        Map<String, String> values = new HashMap<String, String>();
        values.put("errorMessage", HtmlUtil.escape(errorMessage));
        values.put("successMessage", HtmlUtil.escape(successMessage));
        for (int i = 0; i < sections.size(); i++) {
            values.putAll(sections.get(i).getPlaceholders());
        }
        return templates.render("admin.html", values);
    }
}
