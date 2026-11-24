package com.byahero.controller;

import com.byahero.model.RouteStop;
import com.byahero.service.BusService;
import com.byahero.service.RouteService;
import com.byahero.util.FormParser;
import com.byahero.util.HtmlUtil;
import com.byahero.util.TemplateLoader;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

/** Handles GET /route?destination=Manila - shows the stops in order and a Google Maps link. */
public class RouteController extends BaseController {

    private RouteService routeService;
    private BusService busService;

    public RouteController(RouteService routeService, BusService busService) {
        this.routeService = routeService;
        this.busService = busService;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String destination = FormParser.get(readQuery(exchange), "destination");

        String errorMessage = "";
        String rows = "";
        String mapsLink = "";
        int statusCode = 200;

        // only look for stops if the user picked a destination
        if (!destination.isEmpty()) {
            List<RouteStop> stops = routeService.getStops(destination);

            if (stops.isEmpty()) {
                errorMessage = "No route stops found for " + destination + ".";
                statusCode = 404;
            }

            // one table row for each stop
            for (int i = 0; i < stops.size(); i++) {
                RouteStop stop = stops.get(i);
                rows += "<tr><td>" + stop.getStopOrder() + "</td>";
                rows += "<td>" + HtmlUtil.escape(stop.getStopName()) + "</td>";
                rows += "<td>" + HtmlUtil.escape(stop.getRoad()) + "</td></tr>\n";
            }

            String link = routeService.getMapsLink(destination);
            mapsLink = "<a href=\"" + HtmlUtil.escape(link) + "\" target=\"_blank\">Open in Google Maps</a>";
        }

        // fill in the placeholders of route.html
        String html = TemplateLoader.load("route.html");
        html = html.replace("{{errorMessage}}", HtmlUtil.escape(errorMessage));
        html = html.replace("{{destination}}", HtmlUtil.escape(destination));
        html = html.replace("{{destinationOptions}}", HtmlUtil.options(busService.getDestinations(), destination));
        html = html.replace("{{routeRows}}", rows);
        html = html.replace("{{mapsLink}}", mapsLink);
        sendHtml(exchange, statusCode, html);
    }
}
