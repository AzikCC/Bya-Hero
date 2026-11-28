package com.byahero.service;

import com.byahero.model.RouteStop;

import java.util.List;

/** What the route page needs: the stops in order and an embedded map. */
public interface RouteService {

    List<RouteStop> getStops(String destination);

    /** The address of an embeddable Google Map for the destination's bus terminal. */
    String getMapEmbedUrl(String destination);
}
