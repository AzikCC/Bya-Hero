package com.byahero.service;

import com.byahero.model.Fare;

import java.util.List;

/** Admin actions for fares only. */
public interface FareAdminService {

    List<Fare> getAllFares();

    void addFare(String destination, String baseFareText);

    void updateFare(String destination, String baseFareText);

    void deleteFare(String destination);
}
