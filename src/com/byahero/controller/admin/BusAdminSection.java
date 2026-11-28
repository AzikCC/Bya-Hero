package com.byahero.controller.admin;

import com.byahero.model.Bus;
import com.byahero.service.BusAdminService;
import com.byahero.util.FormParser;
import com.byahero.view.HtmlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The "bus" forms and table of the admin page. */
public class BusAdminSection extends BaseAdminSection {

    private final BusAdminService service;

    public BusAdminSection(BusAdminService service) {
        this.service = service;
    }

    @Override
    public String getName() {
        return "bus";
    }

    @Override
    protected String getLabel() {
        return "Bus";
    }

    @Override
    protected void add(Map<String, String> form) {
        service.addBus(FormParser.get(form, "plate"), FormParser.get(form, "busType"),
                FormParser.get(form, "operator"), FormParser.get(form, "destination"),
                FormParser.get(form, "bay"), FormParser.get(form, "estDeparture"));
    }

    @Override
    protected void update(Map<String, String> form) {
        service.updateBus(FormParser.get(form, "plate"), FormParser.get(form, "operator"),
                FormParser.get(form, "destination"), FormParser.get(form, "bay"),
                FormParser.get(form, "estDeparture"));
    }

    @Override
    protected void delete(Map<String, String> form) {
        service.deleteBus(FormParser.get(form, "plate"));
    }

    @Override
    public Map<String, String> getPlaceholders() {
        StringBuilder rows = new StringBuilder();
        List<Bus> buses = service.getAllBuses();
        for (int i = 0; i < buses.size(); i++) {
            Bus bus = buses.get(i);
            rows.append("<tr><td>").append(HtmlUtil.escape(bus.getPlate())).append("</td>");
            rows.append("<td>").append(HtmlUtil.escape(bus.getBusType())).append("</td>");
            rows.append("<td>").append(HtmlUtil.escape(bus.getOperator())).append("</td>");
            rows.append("<td>").append(HtmlUtil.escape(bus.getDestination())).append("</td>");
            rows.append("<td>").append(bus.getStatus()).append("</td>");
            rows.append("<td>").append(HtmlUtil.escape(bus.getBay())).append("</td>");
            rows.append("<td>").append(HtmlUtil.escape(bus.getEstDeparture())).append("</td></tr>\n");
        }
        Map<String, String> values = new HashMap<String, String>();
        values.put("busRows", rows.toString());
        values.put("busTypeOptions", HtmlUtil.options(service.getBusTypes(), "", true));
        // the first (empty) option means "keep the current operator" when updating
        values.put("operatorOptions", "<option value=\"\"></option>" + HtmlUtil.options(service.getOperatorNames(), ""));
        return values;
    }
}
