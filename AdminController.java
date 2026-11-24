package com.byahero.controller;

import com.byahero.model.Bus;
import com.byahero.model.Fare;
import com.byahero.model.RouteStop;
import com.byahero.service.AdminService;
import com.byahero.util.FormParser;
import com.byahero.util.HtmlUtil;
import com.byahero.util.TemplateLoader;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Handles /admin.
 *   GET  = show the tables and forms
 *   POST = run an add / update / delete action
 *
 * Each form on the page sends two special values:
 *   entity = "fare", "route" or "bus"   (what we are changing)
 *   action = "add", "update" or "delete" (what to do with it)
 */
public class AdminController extends BaseController {

    private AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String page = render("", "");
        sendHtml(exchange, 200, page);
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        Map<String, String> form = readFormBody(exchange);
        String entity = FormParser.get(form, "entity");
        String action = FormParser.get(form, "action");

        try {
            String message = "";
            if (entity.equals("fare")) {
                message = handleFare(action, form);
            } else if (entity.equals("route")) {
                message = handleRoute(action, form);
            } else if (entity.equals("bus")) {
                message = handleBus(action, form);
            } else {
                throw new IllegalArgumentException("Unknown admin form.");
            }
            sendHtml(exchange, 200, render("", message));
        } catch (RuntimeException e) {
            sendHtml(exchange, 400, render(e.getMessage(), ""));
        }
    }

    // ---------- one method for each kind of form ----------
    // Each one returns a success message, or throws an error that is shown on the page.

    private String handleFare(String action, Map<String, String> form) {
        String destination = FormParser.get(form, "destination");
        String baseFare = FormParser.get(form, "baseFare");

        if (action.equals("add")) {
            adminService.addFare(destination, baseFare);
            return "Fare added.";
        } else if (action.equals("update")) {
            adminService.updateFare(destination, baseFare);
            return "Fare updated.";
        } else if (action.equals("delete")) {
            adminService.deleteFare(destination);
            return "Fare deleted.";
        }
        throw new IllegalArgumentException("Unknown action.");
    }

    private String handleRoute(String action, Map<String, String> form) {
        String destination = FormParser.get(form, "destination");
        String stopOrder = FormParser.get(form, "stopOrder");
        String stopName = FormParser.get(form, "stopName");
        String road = FormParser.get(form, "road");

        if (action.equals("add")) {
            adminService.addStop(destination, stopOrder, stopName, road);
            return "Route stop added.";
        } else if (action.equals("update")) {
            adminService.updateStop(destination, stopOrder, stopName, road);
            return "Route stop updated.";
        } else if (action.equals("delete")) {
            adminService.deleteStop(destination, stopOrder);
            return "Route stop deleted.";
        }
        throw new IllegalArgumentException("Unknown action.");
    }

    private String handleBus(String action, Map<String, String> form) {
        String plate = FormParser.get(form, "plate");
        String busType = FormParser.get(form, "busType");
        String operator = FormParser.get(form, "operator");
        String destination = FormParser.get(form, "destination");
        String bay = FormParser.get(form, "bay");
        String estDeparture = FormParser.get(form, "estDeparture");

        if (action.equals("add")) {
            adminService.addBus(plate, busType, operator, destination, bay, estDeparture);
            return "Bus added.";
        } else if (action.equals("update")) {
            adminService.updateBus(plate, operator, destination, bay, estDeparture);
            return "Bus updated.";
        } else if (action.equals("delete")) {
            adminService.deleteBus(plate);
            return "Bus deleted.";
        }
        throw new IllegalArgumentException("Unknown action.");
    }

    // ---------- building the page ----------

    private String render(String errorMessage, String successMessage) {
        String html = TemplateLoader.load("admin.html");
        html = html.replace("{{errorMessage}}", HtmlUtil.escape(errorMessage));
        html = html.replace("{{successMessage}}", HtmlUtil.escape(successMessage));
        html = html.replace("{{fareRows}}", buildFareRows());
        html = html.replace("{{routeRows}}", buildRouteRows());
        html = html.replace("{{busRows}}", buildBusRows());
        return html;
    }

    private String buildFareRows() {
        String rows = "";
        List<Fare> fares = adminService.getAllFares();
        for (int i = 0; i < fares.size(); i++) {
            Fare fare = fares.get(i);
            rows += "<tr><td>" + HtmlUtil.escape(fare.getDestination()) + "</td>";
            rows += "<td>" + fare.getBaseFare() + "</td></tr>\n";
        }
        return rows;
    }

    private String buildRouteRows() {
        String rows = "";
        List<RouteStop> stops = adminService.getAllStops();
        for (int i = 0; i < stops.size(); i++) {
            RouteStop stop = stops.get(i);
            rows += "<tr><td>" + HtmlUtil.escape(stop.getDestination()) + "</td>";
            rows += "<td>" + stop.getStopOrder() + "</td>";
            rows += "<td>" + HtmlUtil.escape(stop.getStopName()) + "</td>";
            rows += "<td>" + HtmlUtil.escape(stop.getRoad()) + "</td></tr>\n";
        }
        return rows;
    }

    private String buildBusRows() {
        String rows = "";
        List<Bus> buses = adminService.getAllBuses();
        for (int i = 0; i < buses.size(); i++) {
            Bus bus = buses.get(i);
            rows += "<tr><td>" + HtmlUtil.escape(bus.getPlate()) + "</td>";
            rows += "<td>" + bus.getBusType() + "</td>";
            rows += "<td>" + HtmlUtil.escape(bus.getOperator()) + "</td>";
            rows += "<td>" + HtmlUtil.escape(bus.getDestination()) + "</td>";
            rows += "<td>" + bus.getStatus() + "</td>";
            rows += "<td>" + HtmlUtil.escape(bus.getBay()) + "</td>";
            rows += "<td>" + HtmlUtil.escape(bus.getEstDeparture()) + "</td></tr>\n";
        }
        return rows;
    }
}
