package com.byahero.repository;

import com.byahero.model.RouteStop;

import java.util.List;

/** Read-only view of route stops, with the one extra lookup the app needs. */
public interface RouteReadRepository extends ReadRepository<RouteStop> {

    /** Returns the stops of one destination, sorted by stop number (1, 2, 3...). */
    List<RouteStop> findByDestination(String destination);
}
