package com.byahero.controller;

import com.byahero.model.RouteStop;
import com.byahero.service.DestinationService;
import com.byahero.service.RouteService;
import com.byahero.util.FormParser;
import com.byahero.view.HtmlUtil;
import com.byahero.view.TemplateLoader;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Handles GET /route?destination=Manila - shows the stops in order and an embedded map. */
public class RouteController extends BaseController {

    private final RouteService routeService;
    private final DestinationService destinations;
    private final TemplateLoader templates;

    public RouteController(RouteService routeService, DestinationService destinations, TemplateLoader templates) {
        this.routeService = routeService;
        this.destinations = destinations;
        this.templates = templates;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String destination = FormParser.get(readQuery(exchange), "destination");

        String errorMessage = "";
        StringBuilder rows = new StringBuilder();
        String mapEmbed = "";
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
                rows.append("<tr><td>").append(stop.getStopOrder()).append("</td>");
                rows.append("<td>").append(HtmlUtil.escape(stop.getStopName())).append("</td>");
                rows.append("<td>").append(HtmlUtil.escape(stop.getRoad())).append("</td></tr>\n");
            }

            // show the map only when the destination really has a route
            if (!stops.isEmpty()) {
                String url = routeService.getMapEmbedUrl(destination);
                mapEmbed = "<iframe class=\"map-embed\" title=\"Map of " + HtmlUtil.escape(destination)
                        + " bus terminal\" src=\"" + HtmlUtil.escape(url) + "\" loading=\"lazy\" allowfullscreen></iframe>";
            }
        }

        // fill in the placeholders of route.html
        Map<String, String> values = new HashMap<String, String>();
        values.put("errorMessage", HtmlUtil.escape(errorMessage));
        values.put("destination", HtmlUtil.escape(destination));
        values.put("destinationOptions", HtmlUtil.options(destinations.getDestinations(), destination));
        values.put("routeRows", rows.toString());
        values.put("mapEmbed", mapEmbed);
        sendHtml(exchange, statusCode, templates.render("route.html", values));
    }
}
