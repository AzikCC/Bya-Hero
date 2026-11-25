package com.byahero.controller.admin;

import java.util.Map;

/**
 * INHERITANCE: the add / update / delete switch that every admin section shares.
 * A child only writes what "add", "update" and "delete" mean for its own data.
 */
public abstract class BaseAdminSection implements AdminSection {

    /** Shown in messages, like "Fare" -> "Fare added." */
    protected abstract String getLabel();

    protected abstract void add(Map<String, String> form);

    protected abstract void update(Map<String, String> form);

    protected abstract void delete(Map<String, String> form);

    @Override
    public final String handle(String action, Map<String, String> form) {
        if (action.equals("add")) {
            add(form);
            return getLabel() + " added.";
        }
        if (action.equals("update")) {
            update(form);
            return getLabel() + " updated.";
        }
        if (action.equals("delete")) {
            delete(form);
            return getLabel() + " deleted.";
        }
        throw new IllegalArgumentException("Unknown action.");
    }
}
