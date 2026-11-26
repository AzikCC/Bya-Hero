package com.byahero.service.impl;

import com.byahero.model.RouteStop;
import com.byahero.repository.RouteRepository;
import com.byahero.service.RouteAdminService;
import com.byahero.util.InputParser;

import java.util.List;

public class DefaultRouteAdminService implements RouteAdminService {

    private final RouteRepository routeRepo;

    public DefaultRouteAdminService(RouteRepository routeRepo) {
        this.routeRepo = routeRepo;
    }

    @Override
    public List<RouteStop> getAllStops() {
        return routeRepo.findAll();
    }

    @Override
    public void addStop(String destination, String stopOrderText, String stopName, String road) {
        int stopOrder = InputParser.toInt(stopOrderText, "Stop order");
        routeRepo.save(new RouteStop(destination, stopOrder, stopName, road));
    }

    @Override
    public void updateStop(String destination, String stopOrderText, String stopName, String road) {
        int stopOrder = InputParser.toInt(stopOrderText, "Stop order");
        routeRepo.update(new RouteStop(destination, stopOrder, stopName, road));
    }

    @Override
    public void deleteStop(String destination, String stopOrderText) {
        int stopOrder = InputParser.toInt(stopOrderText, "Stop order");
        if (!routeRepo.delete(destination + "#" + stopOrder)) {
            throw new IllegalArgumentException("No stop " + stopOrder + " found for " + destination + ".");
        }
    }
}
