package com.byahero.controller.admin;

import com.byahero.model.Operator;
import com.byahero.service.OperatorAdminService;
import com.byahero.util.FormParser;
import com.byahero.view.HtmlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The "operator" forms and table of the admin page. */
public class OperatorAdminSection extends BaseAdminSection {

    private final OperatorAdminService service;

    public OperatorAdminSection(OperatorAdminService service) {
        this.service = service;
    }

    @Override
    public String getName() {
        return "operator";
    }

    @Override
    protected String getLabel() {
        return "Operator";
    }

    @Override
    protected void add(Map<String, String> form) {
        service.addOperator(FormParser.get(form, "name"), FormParser.get(form, "contactNumber"));
    }

    @Override
    protected void update(Map<String, String> form) {
        service.updateOperator(FormParser.get(form, "name"), FormParser.get(form, "contactNumber"));
    }

    @Override
    protected void delete(Map<String, String> form) {
        service.deleteOperator(FormParser.get(form, "name"));
    }

    @Override
    public Map<String, String> getPlaceholders() {
        StringBuilder rows = new StringBuilder();
        List<Operator> operators = service.getAllOperators();
        for (int i = 0; i < operators.size(); i++) {
            Operator operator = operators.get(i);
            rows.append("<tr><td>").append(HtmlUtil.escape(operator.getName())).append("</td>");
            rows.append("<td>").append(HtmlUtil.escape(operator.getContactNumber())).append("</td></tr>\n");
        }
        Map<String, String> values = new HashMap<String, String>();
        values.put("operatorRows", rows.toString());
        return values;
    }
}
