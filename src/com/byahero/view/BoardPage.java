package com.byahero.view;

import com.byahero.model.Bus;
import com.byahero.model.BusStatus;
import com.byahero.service.BoardService;
import com.byahero.service.FareService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SINGLE RESPONSIBILITY: builds the HTML of the public board and nothing else.
 * (It used to live inside BoardController, which also had to handle requests,
 * and StatusController had to depend on that whole controller just to reuse it.)
 *
 * Each bus has its own "bay" with its fare, status and a countdown timer.
 */
public class BoardPage {

    private final TemplateLoader templates;
    private final BoardService boardService;
    private final FareService fareService;

    public BoardPage(TemplateLoader templates, BoardService boardService, FareService fareService) {
        this.templates = templates;
        this.boardService = boardService;
        this.fareService = fareService;
    }

    /** Builds the whole board page, with an optional error message at the top. */
    public String render(String errorMessage) {
        Map<String, String> values = new HashMap<String, String>();
        values.put("errorMessage", HtmlUtil.escape(errorMessage));
        values.put("boardContent", buildBoardContent());
        return templates.render("board.html", values);
    }

    /** Builds the HTML for all destinations and their buses. */
    private String buildBoardContent() {
        Map<String, List<Bus>> groups = boardService.getActiveBusesByDestination();

        if (groups.isEmpty()) {
            return "<p class=\"empty\">No buses are in the terminal. <a href=\"/checkin\">Check in a bus</a> to see it here.</p>";
        }

        StringBuilder html = new StringBuilder("<div class=\"board\">\n");

        // one section for each destination
        for (String destination : groups.keySet()) {
            List<Bus> buses = groups.get(destination);

            // read the base fare once per destination, not once per bus
            double baseFare = fareService.getBaseFare(destination);

            html.append("<section class=\"lane\">\n");
            html.append("<header class=\"lane-sign\"><h2>").append(HtmlUtil.escape(destination)).append("</h2>");
            if (baseFare >= 0) {
                html.append("<p>Base fare &#8369;").append(HtmlUtil.money(baseFare)).append("</p>");
            }
            html.append("<a href=\"/route?destination=").append(HtmlUtil.urlEncode(destination))
                .append("\">View route</a></header>\n");

            // one bay for each bus going to this destination
            html.append("<div class=\"bays\">\n");
            for (int i = 0; i < buses.size(); i++) {
                html.append(buildBay(buses.get(i), baseFare));
            }
            html.append("</div>\n</section>\n");
        }

        html.append("</div>\n");
        return html.toString();
    }

    /** Builds the HTML of ONE bus: the bay number, the bus picture and the timer sign. */
    private String buildBay(Bus bus, double baseFare) {
        // values used for the CSS class names, like "type-provincial" and "status-loading"
        String type = bus.getBusType().toLowerCase();
        String status = bus.getStatus().name().toLowerCase();
        String typeName = HtmlUtil.capitalize(type);

        // text from the bus must be escaped before it goes into HTML
        String plate = HtmlUtil.escape(bus.getPlate());
        String operator = HtmlUtil.escape(bus.getOperator());
        String bay = HtmlUtil.escape(bus.getBay());
        String estDeparture = HtmlUtil.escape(bus.getEstDeparture());

        // POLYMORPHISM: the bus runs the fare formula of its own type
        String fareText;
        if (baseFare < 0) {
            fareText = "Fare not set";
        } else {
            fareText = "&#8369;" + HtmlUtil.money(bus.calculateFare(baseFare));
        }

        // the button moves the bus to its next status
        BusStatus nextStatus = bus.getStatus().next();
        String buttonText = "Mark departed";
        if (nextStatus == BusStatus.LOADING) {
            buttonText = "Start loading";
        }

        StringBuilder html = new StringBuilder();
        html.append("<article class=\"bay type-").append(type).append(" status-").append(status)
            .append("\" data-depart=\"").append(estDeparture).append("\">\n");
        html.append("  <p class=\"bay-no\">Bay ").append(bay).append("</p>\n");

        // the bus picture (drawn with CSS)
        html.append("  <div class=\"bus\" role=\"img\" aria-label=\"").append(typeName).append(" bus ")
            .append(plate).append(", operated by ").append(operator).append("\">");
        html.append("<div class=\"roof\"><span class=\"operator\">").append(operator).append("</span></div>");
        html.append("<div class=\"windshield\"></div>");
        html.append("<span class=\"plate\">").append(plate).append("</span></div>\n");

        // the timer sign in front of the bus (board.js fills in the countdown)
        html.append("  <div class=\"timer-sign\">\n");
        html.append("    <p class=\"count\" aria-hidden=\"true\" data-count>").append(estDeparture).append("</p>\n");
        html.append("    <p class=\"caption\" data-caption>Departs at ").append(estDeparture).append("</p>\n");
        html.append("    <span class=\"status\">").append(HtmlUtil.capitalize(status)).append("</span>\n");
        html.append("    <p class=\"fare\">").append(typeName).append(", ").append(fareText).append("</p>\n");

        // the button submits to POST /status
        html.append("    <form method=\"post\" action=\"/status\">");
        html.append("<input type=\"hidden\" name=\"plate\" value=\"").append(plate).append("\">");
        html.append("<input type=\"hidden\" name=\"nextStatus\" value=\"").append(nextStatus).append("\">");
        html.append("<button type=\"submit\">").append(buttonText).append("</button></form>\n");
        html.append("  </div>\n");
        html.append("</article>\n");
        return html.toString();
    }
}
