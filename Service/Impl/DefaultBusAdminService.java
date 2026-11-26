package com.byahero.service.impl;

import com.byahero.model.Bus;
import com.byahero.repository.Repository;
import com.byahero.service.BusAdminService;
import com.byahero.service.CheckInService;
import com.byahero.service.DestinationService;
import com.byahero.service.OperatorService;
import com.byahero.util.Validator;

import java.util.List;

/** Admin actions for buses. Adding a bus reuses the check-in rules instead of copying them. */
public class DefaultBusAdminService implements BusAdminService {

    private final Repository<Bus> busRepo;
    private final CheckInService checkInService;
    private final DestinationService destinations;
    private final OperatorService operators;

    public DefaultBusAdminService(Repository<Bus> busRepo, CheckInService checkInService,
                                  DestinationService destinations, OperatorService operators) {
        this.busRepo = busRepo;
        this.checkInService = checkInService;
        this.destinations = destinations;
        this.operators = operators;
    }

    @Override
    public List<Bus> getAllBuses() {
        return busRepo.findAll();
    }

    @Override
    public List<String> getBusTypes() {
        return checkInService.getBusTypes();
    }

    @Override
    public List<String> getOperatorNames() {
        return operators.getOperatorNames();
    }

    @Override
    public void addBus(String plate, String busType, String operator, String destination,
                       String bay, String estDeparture) {
        checkInService.checkIn(plate, busType, operator, destination, bay, estDeparture);
    }

    @Override
    public void updateBus(String plate, String operator, String destination, String bay, String estDeparture) {
        Bus bus = busRepo.findById(Validator.clean(plate));
        if (bus == null) {
            throw new IllegalArgumentException("No bus found with plate " + Validator.clean(plate) + ".");
        }

        if (!Validator.isBlank(operator)) {
            String knownOperator = operators.findKnownOperator(operator);
            if (knownOperator == null) {
                throw new IllegalArgumentException("Unknown operator '" + operator.trim() + "'. Add it on the Admin page first.");
            }
            bus.setOperator(knownOperator);
        }
        if (!Validator.isBlank(destination)) {
            String knownDestination = destinations.findKnownDestination(destination);
            if (knownDestination == null) {
                throw new IllegalArgumentException("Unknown destination '" + destination.trim() + "'.");
            }
            bus.setDestination(knownDestination);
        }
        if (!Validator.isBlank(bay)) {
            bus.setBay(bay);
        }
        if (!Validator.isBlank(estDeparture)) {
            bus.setEstDeparture(estDeparture);
        }
        busRepo.update(bus);
    }

    @Override
    public void deleteBus(String plate) {
        if (!busRepo.delete(Validator.clean(plate))) {
            throw new IllegalArgumentException("No bus found with plate " + Validator.clean(plate) + ".");
        }
    }
}
