package com.byahero.service.impl;

import com.byahero.model.Fare;
import com.byahero.repository.ReadRepository;
import com.byahero.service.FareService;

public class DefaultFareService implements FareService {

    private final ReadRepository<Fare> fareRepo;

    public DefaultFareService(ReadRepository<Fare> fareRepo) {
        this.fareRepo = fareRepo;
    }

    @Override
    public double getBaseFare(String destination) {
        Fare fare = fareRepo.findById(destination);
        if (fare == null) {
            return -1;
        }
        return fare.getBaseFare();
    }
}
