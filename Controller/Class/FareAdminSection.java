package com.byahero.controller.admin;

import com.byahero.model.Fare;
import com.byahero.service.FareAdminService;
import com.byahero.util.FormParser;
import com.byahero.view.HtmlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The "fare" forms and table of the admin page. */
public class FareAdminSection extends BaseAdminSection {

    private final FareAdminService service;

    public FareAdminSection(FareAdminService service) {
        this.service = service;
    }

    @Override
    public String getName() {
        return "fare";
    }

    @Override
    protected String getLabel() {
        return "Fare";
    }

    @Override
    protected void add(Map<String, String> form) {
        service.addFare(FormParser.get(form, "destination"), FormParser.get(form, "baseFare"));
    }

    @Override
    protected void update(Map<String, String> form) {
        service.updateFare(FormParser.get(form, "destination"), FormParser.get(form, "baseFare"));
    }

    @Override
    protected void delete(Map<String, String> form) {
        service.deleteFare(FormParser.get(form, "destination"));
    }

    @Override
    public Map<String, String> getPlaceholders() {
        StringBuilder rows = new StringBuilder();
        List<Fare> fares = service.getAllFares();
        for (int i = 0; i < fares.size(); i++) {
            Fare fare = fares.get(i);
            rows.append("<tr><td>").append(HtmlUtil.escape(fare.getDestination())).append("</td>");
            rows.append("<td>").append(fare.getBaseFare()).append("</td></tr>\n");
        }
        Map<String, String> values = new HashMap<String, String>();
        values.put("fareRows", rows.toString());
        return values;
    }
}
