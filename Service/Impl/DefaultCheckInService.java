package com.byahero.service.impl;

import com.byahero.exception.DuplicateBusException;
import com.byahero.exception.InvalidPlateException;
import com.byahero.model.Bus;
import com.byahero.model.BusFactory;
import com.byahero.model.BusStatus;
import com.byahero.repository.Repository;
import com.byahero.service.CheckInService;
import com.byahero.service.DestinationService;
import com.byahero.service.OperatorService;
import com.byahero.util.DateUtil;
import com.byahero.util.Validator;

import java.util.List;

/**
 * DEPENDENCY INVERSION: everything this class needs is handed in through the
 * constructor as an interface (or the factory). It does not know about CSV files,
 * so tests can pass in a fake in-memory repository instead.
 */
public class DefaultCheckInService implements CheckInService {

    private final Repository<Bus> busRepo;
    private final DestinationService destinations;
    private final OperatorService operators;
    private final BusFactory busFactory;

    public DefaultCheckInService(Repository<Bus> busRepo, DestinationService destinations,
                                 OperatorService operators, BusFactory busFactory) {
        this.busRepo = busRepo;
        this.destinations = destinations;
        this.operators = operators;
        this.busFactory = busFactory;
    }

    /**
     * The checks happen in this order:
     *   1. no blank fields
     *   2. plate format is valid
     *   3. the plate is not already active in the terminal
     *   4. the destination exists
     *   5. the operator is registered (added on the Admin page)
     * If everything is fine the bus is saved with status ARRIVED.
     */
    @Override
    public Bus checkIn(String plate, String busType, String operator,
                       String destination, String bay, String estDeparture) {

        // clean the inputs first
        plate = Validator.clean(plate).toUpperCase();
        busType = Validator.clean(busType);
        operator = Validator.clean(operator);
        destination = Validator.clean(destination);
        bay = Validator.clean(bay);
        estDeparture = Validator.clean(estDeparture);

        // 1. no blank fields
        Validator.requireField(plate, "plate");
        Validator.requireField(busType, "bus type");
        Validator.requireField(operator, "operator");
        Validator.requireField(destination, "destination");
        Validator.requireField(bay, "bay");
        Validator.requireField(estDeparture, "estimated departure");

        // 2. plate format
        if (!Bus.isValidPlate(plate)) {
            throw new InvalidPlateException("Invalid plate '" + plate + "'. Use a format like ABC-123 or ABC-1234.");
        }

        // 3. no duplicate active plate
        Bus existing = busRepo.findById(plate);
        if (existing != null && existing.isActive()) {
            throw new DuplicateBusException("Bus " + plate + " is already in the terminal (" + existing.getStatus() + ").");
        }

        // 4. destination must exist in fares.csv or routes.csv
        String knownDestination = destinations.findKnownDestination(destination);
        if (knownDestination == null) {
            throw new IllegalArgumentException("Unknown destination '" + destination + "'.");
        }

        // 5. operator must be registered in operators.csv
        String knownOperator = operators.findKnownOperator(operator);
        if (knownOperator == null) {
            throw new IllegalArgumentException("Unknown operator '" + operator + "'. Add it on the Admin page first.");
        }

        // everything is fine: create the bus and save it
        Bus bus = busFactory.create(busType, plate, knownOperator, knownDestination,
                BusStatus.ARRIVED, bay, DateUtil.now(), estDeparture);
        busRepo.save(bus);
        return bus;
    }

    @Override
    public List<String> getBusTypes() {
        return busFactory.getTypeNames();
    }
}
