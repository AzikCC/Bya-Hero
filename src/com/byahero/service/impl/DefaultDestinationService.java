package com.byahero.service.impl;

import com.byahero.model.Fare;
import com.byahero.model.RouteStop;
import com.byahero.repository.ReadRepository;
import com.byahero.repository.RouteReadRepository;
import com.byahero.service.DestinationService;
import com.byahero.util.Validator;

import java.util.ArrayList;
import java.util.List;

/** DEPENDENCY INVERSION: needs only READ access, and only to interfaces. */
public class DefaultDestinationService implements DestinationService {

    private final ReadRepository<Fare> fareRepo;
    private final RouteReadRepository routeRepo;

    public DefaultDestinationService(ReadRepository<Fare> fareRepo, RouteReadRepository routeRepo) {
        this.fareRepo = fareRepo;
        this.routeRepo = routeRepo;
    }

    @Override
    public List<String> getDestinations() {
        List<String> names = new ArrayList<String>();
        List<Fare> fares = fareRepo.findAll();
        for (int i = 0; i < fares.size(); i++) {
            names.add(fares.get(i).getDestination());
        }
        return names;
    }

    @Override
    public String findKnownDestination(String destination) {
        if (Validator.isBlank(destination)) {
            return null;
        }

        Fare fare = fareRepo.findById(destination);
        if (fare != null) {
            return fare.getDestination();
        }

        List<RouteStop> stops = routeRepo.findByDestination(destination);
        if (!stops.isEmpty()) {
            return stops.get(0).getDestination();
        }
        return null;
    }
}
