package com.byahero.service;

import java.util.List;

/** Knows which destinations exist (in fares.csv or routes.csv). */
public interface DestinationService {

    /** All destination names that have a fare (used for dropdown lists). */
    List<String> getDestinations();

    /**
     * Returns the spelling saved in the file (so "manila" becomes "Manila"),
     * or null if the destination is unknown.
     */
    String findKnownDestination(String destination);
}
