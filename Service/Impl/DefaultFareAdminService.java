package com.byahero.service.impl;

import com.byahero.model.Fare;
import com.byahero.repository.Repository;
import com.byahero.service.FareAdminService;
import com.byahero.util.InputParser;

import java.util.List;

public class DefaultFareAdminService implements FareAdminService {

    private final Repository<Fare> fareRepo;

    public DefaultFareAdminService(Repository<Fare> fareRepo) {
        this.fareRepo = fareRepo;
    }

    @Override
    public List<Fare> getAllFares() {
        return fareRepo.findAll();
    }

    @Override
    public void addFare(String destination, String baseFareText) {
        double baseFare = InputParser.toDouble(baseFareText, "Base fare");
        fareRepo.save(new Fare(destination, baseFare));
    }

    @Override
    public void updateFare(String destination, String baseFareText) {
        double baseFare = InputParser.toDouble(baseFareText, "Base fare");
        fareRepo.update(new Fare(destination, baseFare));
    }

    @Override
    public void deleteFare(String destination) {
        if (!fareRepo.delete(destination)) {
            throw new IllegalArgumentException("No fare found for " + destination + ".");
        }
    }
}
