package com.byahero.controller.admin;

import com.byahero.model.AdminAccount;
import com.byahero.service.AdminAccountService;
import com.byahero.util.FormParser;
import com.byahero.view.HtmlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The "admin account" form and table of the admin page.
 *   add    = new admin with a password
 *   update = set a new password for an existing admin
 *   delete = remove an admin (the last one cannot be removed)
 * Passwords are never shown: the table only lists usernames.
 */
public class AdminAccountSection extends BaseAdminSection {

    private final AdminAccountService service;

    public AdminAccountSection(AdminAccountService service) {
        this.service = service;
    }

    @Override
    public String getName() {
        return "account";
    }

    @Override
    protected String getLabel() {
        return "Admin account";
    }

    @Override
    protected void add(Map<String, String> form) {
        service.addAccount(FormParser.get(form, "username"), form.get("password"));
    }

    @Override
    protected void update(Map<String, String> form) {
        service.changePassword(FormParser.get(form, "username"), form.get("password"));
    }

    @Override
    protected void delete(Map<String, String> form) {
        service.deleteAccount(FormParser.get(form, "username"));
    }

    @Override
    public Map<String, String> getPlaceholders() {
        StringBuilder rows = new StringBuilder();
        List<AdminAccount> accounts = service.getAllAccounts();
        for (int i = 0; i < accounts.size(); i++) {
            rows.append("<tr><td>").append(HtmlUtil.escape(accounts.get(i).getUsername())).append("</td></tr>\n");
        }
        Map<String, String> values = new HashMap<String, String>();
        values.put("accountRows", rows.toString());
        return values;
    }
}
