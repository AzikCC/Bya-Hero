package com.byahero.controller.admin;

import java.util.Map;

/**
 * OPEN/CLOSED + INTERFACE SEGREGATION: one admin feature (fares, route stops, buses...).
 * AdminController only knows this small interface. To add a new admin feature you
 * write a new AdminSection and register it in AppConfig; AdminController is not edited.
 */
public interface AdminSection {

    /** The value of the form's hidden "entity" field, like "fare". */
    String getName();

    /** Runs "add", "update" or "delete" and returns a success message. Throws if something is wrong. */
    String handle(String action, Map<String, String> form);

    /** The {{placeholders}} this section fills in on admin.html, like {"fareRows" -> "<tr>..."}. */
    Map<String, String> getPlaceholders();
}
