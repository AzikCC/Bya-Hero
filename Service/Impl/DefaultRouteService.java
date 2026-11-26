package com.byahero.service.impl;

import com.byahero.model.RouteStop;
import com.byahero.repository.RouteReadRepository;
import com.byahero.service.RouteService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DefaultRouteService implements RouteService {

    private final RouteReadRepository routeRepo;

    public DefaultRouteService(RouteReadRepository routeRepo) {
        this.routeRepo = routeRepo;
    }

    @Override
    public List<RouteStop> getStops(String destination) {
        return routeRepo.findByDestination(destination);
    }

    /** Builds an embeddable Google Maps address. It needs no API key. */
    @Override
    public String getMapEmbedUrl(String destination) {
        String search = URLEncoder.encode(destination + " bus terminal", StandardCharsets.UTF_8);
        return "https://www.google.com/maps?q=" + search + "&output=embed";
    }
}
