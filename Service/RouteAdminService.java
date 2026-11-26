package com.byahero.service;

import com.byahero.model.RouteStop;

import java.util.List;

/** Admin actions for route stops only. */
public interface RouteAdminService {

    List<RouteStop> getAllStops();

    void addStop(String destination, String stopOrderText, String stopName, String road);

    void updateStop(String destination, String stopOrderText, String stopName, String road);

    void deleteStop(String destination, String stopOrderText);
}
