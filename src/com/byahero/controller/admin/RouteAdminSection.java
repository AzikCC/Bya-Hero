package com.byahero.controller.admin;

import com.byahero.model.RouteStop;
import com.byahero.service.RouteAdminService;
import com.byahero.util.FormParser;
import com.byahero.view.HtmlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The "route" forms and table of the admin page. */
public class RouteAdminSection extends BaseAdminSection {

    private final RouteAdminService service;

    public RouteAdminSection(RouteAdminService service) {
        this.service = service;
    }

    @Override
    public String getName() {
        return "route";
    }

    @Override
    protected String getLabel() {
        return "Route stop";
    }

    @Override
    protected void add(Map<String, String> form) {
        service.addStop(FormParser.get(form, "destination"), FormParser.get(form, "stopOrder"),
                FormParser.get(form, "stopName"), FormParser.get(form, "road"));
    }

    @Override
    protected void update(Map<String, String> form) {
        service.updateStop(FormParser.get(form, "destination"), FormParser.get(form, "stopOrder"),
                FormParser.get(form, "stopName"), FormParser.get(form, "road"));
    }

    @Override
    protected void delete(Map<String, String> form) {
        service.deleteStop(FormParser.get(form, "destination"), FormParser.get(form, "stopOrder"));
    }

    @Override
    public Map<String, String> getPlaceholders() {
        StringBuilder rows = new StringBuilder();
        List<RouteStop> stops = service.getAllStops();
        for (int i = 0; i < stops.size(); i++) {
            RouteStop stop = stops.get(i);
            rows.append("<tr><td>").append(HtmlUtil.escape(stop.getDestination())).append("</td>");
            rows.append("<td>").append(stop.getStopOrder()).append("</td>");
            rows.append("<td>").append(HtmlUtil.escape(stop.getStopName())).append("</td>");
            rows.append("<td>").append(HtmlUtil.escape(stop.getRoad())).append("</td></tr>\n");
        }
        Map<String, String> values = new HashMap<String, String>();
        values.put("routeRows", rows.toString());
        return values;
    }
}
