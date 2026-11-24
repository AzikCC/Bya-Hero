package com.byahero.controller;

import com.byahero.model.Bus;
import com.byahero.model.BusStatus;
import com.byahero.service.BusService;
import com.byahero.util.HtmlUtil;
import com.byahero.util.TemplateLoader;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Handles GET /board - the public board.
 * It shows the active buses grouped by destination.
 * Each bus has its own "bay" with its fare, status and a countdown timer.
 */
public class BoardController extends BaseController {

    private BusService busService;

    public BoardController(BusService busService) {
        this.busService = busService;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String page = render("");
        sendHtml(exchange, 200, page);
    }

    /**
     * Builds the whole board page. StatusController also calls this
     * to show the board together with an error message.
     */
    public String render(String errorMessage) {
        String html = TemplateLoader.load("board.html");
        html = html.replace("{{errorMessage}}", HtmlUtil.escape(errorMessage));
        html = html.replace("{{boardContent}}", buildBoardContent());
        return html;
    }

    /** Builds the HTML for all destinations and their buses. */
    private String buildBoardContent() {
        Map<String, List<Bus>> groups = busService.getActiveBusesByDestination();

        if (groups.isEmpty()) {
            return "<p class=\"empty\">No buses are in the terminal. <a href=\"/checkin\">Check in a bus</a> to see it here.</p>";
        }

        String html = "<div class=\"board\">\n";

        // one section for each destination
        for (String destination : groups.keySet()) {
            List<Bus> buses = groups.get(destination);

            html += "<section class=\"lane\">\n";
            html += "<header class=\"lane-sign\"><h2>" + HtmlUtil.escape(destination) + "</h2>";

            double baseFare = busService.getBaseFare(destination);
            if (baseFare >= 0) {
                html += "<p>Base fare &#8369;" + money(baseFare) + "</p>";
            }
            html += "<a href=\"/route?destination=" + HtmlUtil.urlEncode(destination) + "\">View route</a></header>\n";

            // one bay for each bus going to this destination
            html += "<div class=\"bays\">\n";
            for (int i = 0; i < buses.size(); i++) {
                html += buildBay(buses.get(i));
            }
            html += "</div>\n</section>\n";
        }

        html += "</div>\n";
        return html;
    }

    /** Builds the HTML of ONE bus: the bay number, the bus picture and the timer sign. */
    private String buildBay(Bus bus) {
        // values used for the CSS class names, like "type-aircon" and "status-loading"
        String type = bus.getBusType().toLowerCase();
        String status = bus.getStatus().name().toLowerCase();
        String typeName = capitalize(type);

        // text from the bus must be escaped before it goes into HTML
        String plate = HtmlUtil.escape(bus.getPlate());
        String operator = HtmlUtil.escape(bus.getOperator());
        String bay = HtmlUtil.escape(bus.getBay());
        String estDeparture = HtmlUtil.escape(bus.getEstDeparture());

        // the fare comes from the bus itself (polymorphism, see BusService.computeFare)
        String fareText;
        double fare = busService.computeFare(bus);
        if (fare < 0) {
            fareText = "Fare not set";
        } else {
            fareText = "&#8369;" + money(fare);
        }

        // the button moves the bus to its next status
        BusStatus nextStatus = bus.getStatus().next();
        String buttonText;
        if (nextStatus == BusStatus.LOADING) {
            buttonText = "Start loading";
        } else {
            buttonText = "Mark departed";
        }

        String html = "";
        html += "<article class=\"bay type-" + type + " status-" + status + "\" data-depart=\"" + estDeparture + "\">\n";
        html += "  <p class=\"bay-no\">Bay " + bay + "</p>\n";

        // the bus picture (drawn with CSS)
        html += "  <div class=\"bus\" role=\"img\" aria-label=\"" + typeName + " bus " + plate + ", operated by " + operator + "\">";
        html += "<div class=\"roof\"><span class=\"operator\">" + operator + "</span></div>";
        html += "<div class=\"windshield\"></div>";
        html += "<span class=\"plate\">" + plate + "</span></div>\n";

        // the timer sign in front of the bus (board.js fills in the countdown)
        html += "  <div class=\"timer-sign\">\n";
        html += "    <p class=\"count\" aria-hidden=\"true\" data-count>" + estDeparture + "</p>\n";
        html += "    <p class=\"caption\" data-caption>Departs at " + estDeparture + "</p>\n";
        html += "    <span class=\"status\">" + capitalize(status) + "</span>\n";
        html += "    <p class=\"fare\">" + typeName + ", " + fareText + "</p>\n";

        // the button submits to POST /status
        html += "    <form method=\"post\" action=\"/status\">";
        html += "<input type=\"hidden\" name=\"plate\" value=\"" + plate + "\">";
        html += "<input type=\"hidden\" name=\"nextStatus\" value=\"" + nextStatus + "\">";
        html += "<button type=\"submit\">" + buttonText + "</button></form>\n";
        html += "  </div>\n";
        html += "</article>\n";
        return html;
    }

    /** Formats a number with 2 decimals, like 96.0 -> "96.00". */
    private String money(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

    /** Makes the first letter big and the rest small, like "aircon" -> "Aircon". */
    private String capitalize(String text) {
        String first = text.substring(0, 1).toUpperCase();
        String rest = text.substring(1).toLowerCase();
        return first + rest;
    }
}
