package com.byahero.service;

import com.byahero.model.Bus;

import java.util.List;

/** Admin actions for buses only. */
public interface BusAdminService {

    List<Bus> getAllBuses();

    List<String> getBusTypes();

    List<String> getOperatorNames();

    void addBus(String plate, String busType, String operator, String destination, String bay, String estDeparture);

    /** A blank value means "keep the old value". */
    void updateBus(String plate, String operator, String destination, String bay, String estDeparture);

    void deleteBus(String plate);
}
