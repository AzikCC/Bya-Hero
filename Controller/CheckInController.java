package com.byahero.controller;

import com.byahero.model.Bus;
import com.byahero.service.CheckInService;
import com.byahero.service.DestinationService;
import com.byahero.service.OperatorService;
import com.byahero.util.FormParser;
import com.byahero.view.HtmlUtil;
import com.byahero.view.TemplateLoader;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles /checkin.
 *   GET  = show the empty check-in form
 *   POST = read the form, check in the bus, show a success or error message
 */
public class CheckInController extends BaseController {

    private final CheckInService checkInService;
    private final DestinationService destinations;
    private final OperatorService operators;
    private final TemplateLoader templates;

    public CheckInController(CheckInService checkInService, DestinationService destinations,
                             OperatorService operators, TemplateLoader templates) {
        this.checkInService = checkInService;
        this.destinations = destinations;
        this.operators = operators;
        this.templates = templates;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        sendHtml(exchange, 200, render("", "", new HashMap<String, String>()));
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
            Bus bus = checkInService.checkIn(plate, busType, operator, destination, bay, estDeparture);
            String success = "Bus " + bus.getPlate() + " checked in as ARRIVED.";
            sendHtml(exchange, 200, render("", success, new HashMap<String, String>()));
        } catch (RuntimeException e) {
            // show the form again, keeping what the user typed, with an error message
            sendHtml(exchange, 400, render(e.getMessage(), "", form));
        }
    }

    /** Fills in the {{placeholders}} of checkin.html. "typed" holds what the user already entered. */
    private String render(String error, String success, Map<String, String> typed) {
        Map<String, String> values = new HashMap<String, String>();
        values.put("errorMessage", HtmlUtil.escape(error));
        values.put("successMessage", HtmlUtil.escape(success));
        values.put("plate", HtmlUtil.escape(FormParser.get(typed, "plate")));
        values.put("bay", HtmlUtil.escape(FormParser.get(typed, "bay")));
        values.put("estDeparture", HtmlUtil.escape(FormParser.get(typed, "estDeparture")));
        values.put("busTypeOptions", HtmlUtil.options(checkInService.getBusTypes(),
                FormParser.get(typed, "busType"), true));
        values.put("operatorOptions", HtmlUtil.options(operators.getOperatorNames(),
                FormParser.get(typed, "operator")));
        values.put("destinationOptions", HtmlUtil.options(destinations.getDestinations(),
                FormParser.get(typed, "destination")));
        return templates.render("checkin.html", values);
    }
}
