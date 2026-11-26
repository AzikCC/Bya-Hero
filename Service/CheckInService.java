package com.byahero.service;

import com.byahero.model.Bus;

import java.util.List;

/** What the check-in page needs: check a bus in, and list the bus types to choose from. */
public interface CheckInService {

    /** Validates and saves a new bus with status ARRIVED. Throws IllegalArgumentException (or a subclass) if invalid. */
    Bus checkIn(String plate, String busType, String operator,
                String destination, String bay, String estDeparture);

    /** The bus types that can be checked in, like [ORDINARY, PROVINCIAL]. */
    List<String> getBusTypes();
}
