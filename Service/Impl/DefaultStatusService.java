package com.byahero.service.impl;

import com.byahero.model.Bus;
import com.byahero.model.BusStatus;
import com.byahero.repository.Repository;
import com.byahero.service.StatusService;
import com.byahero.util.Validator;

public class DefaultStatusService implements StatusService {

    private final Repository<Bus> busRepo;

    public DefaultStatusService(Repository<Bus> busRepo) {
        this.busRepo = busRepo;
    }

    @Override
    public void updateStatus(String plate, String nextStatusText) {
        plate = Validator.clean(plate).toUpperCase();
        Validator.requireField(plate, "plate");

        Bus bus = busRepo.findById(plate);
        if (bus == null) {
            throw new IllegalArgumentException("No bus found with plate " + plate + ".");
        }

        BusStatus nextStatus = BusStatus.fromString(nextStatusText);
        bus.changeStatus(nextStatus);   // throws an error if the move is not allowed
        busRepo.update(bus);
    }
}
