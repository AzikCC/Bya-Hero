package com.byahero.controller;

import com.byahero.model.Bus;
import com.byahero.service.BusService;
import com.byahero.util.FormParser;
import com.byahero.util.HtmlUtil;
import com.byahero.util.TemplateLoader;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/**
 * Handles /checkin.
 *   GET  = show the empty check-in form
 *   POST = read the form, check in the bus, show a success or error message
 */
public class CheckInController extends BaseController {

    private BusService busService;

    public CheckInController(BusService busService) {
        this.busService = busService;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String page = render("", "", "", "", "", "", "");
        sendHtml(exchange, 200, page);
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        // read every field of the form
        Map<String, String> form = readFormBody(exchange);
        String plate = FormParser.get(form, "plate");
        String busType = FormParser.get(form, "busType");
        String operator = FormParser.get(form, "operator");
        String destination = FormParser.get(form, "destination");
        String bay = FormParser.get(form, "bay");
        String estDeparture = FormParser.get(form, "estDeparture");

        try {
            Bus bus = busService.checkIn(plate, busType, operator, destination, bay, estDeparture);
            String success = "Bus " + bus.getPlate() + " checked in as ARRIVED.";
            String page = render("", success, "", "", "", "", "");
            sendHtml(exchange, 200, page);
        } catch (RuntimeException e) {
            // show the form again, keeping what the user typed, with an error message
            String page = render(e.getMessage(), "", plate, operator, bay, estDeparture, destination);
            sendHtml(exchange, 400, page);
        }
    }

    /** Fills in the {{placeholders}} of checkin.html. */
    private String render(String error, String success, String plate, String operator,
                          String bay, String estDeparture, String destination) {
        String html = TemplateLoader.load("checkin.html");
        html = html.replace("{{errorMessage}}", HtmlUtil.escape(error));
        html = html.replace("{{successMessage}}", HtmlUtil.escape(success));
        html = html.replace("{{plate}}", HtmlUtil.escape(plate));
        html = html.replace("{{operator}}", HtmlUtil.escape(operator));
        html = html.replace("{{bay}}", HtmlUtil.escape(bay));
        html = html.replace("{{estDeparture}}", HtmlUtil.escape(estDeparture));
        html = html.replace("{{destinationOptions}}", HtmlUtil.options(busService.getDestinations(), destination));
        return html;
    }
}
