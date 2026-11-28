package com.byahero.model;

import com.byahero.util.Validator;

/** The base fare for one destination (one row of fares.csv). */
public class Fare {

    private String destination;
    private double baseFare;

    public Fare(String destination, double baseFare) {
        setDestination(destination);
        setBaseFare(baseFare);
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        if (Validator.isBlank(destination)) {
            throw new IllegalArgumentException("Destination must not be blank.");
        }
        this.destination = destination.trim();
    }

    public double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(double baseFare) {
        if (baseFare < 0) {
            throw new IllegalArgumentException("Base fare cannot be negative.");
        }
        this.baseFare = baseFare;
    }
}
