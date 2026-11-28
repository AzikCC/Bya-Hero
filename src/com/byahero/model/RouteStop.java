package com.byahero.model;

import com.byahero.util.Validator;

/** One stop on the way to a destination (one row of routes.csv). */
public class RouteStop {

    private String destination;
    private int stopOrder;
    private String stopName;
    private String road;

    public RouteStop(String destination, int stopOrder, String stopName, String road) {
        setDestination(destination);
        setStopOrder(stopOrder);
        setStopName(stopName);
        setRoad(road);
    }

    /** A unique name for this stop, like "Manila#2" (destination + stop number). */
    public String getId() {
        return destination + "#" + stopOrder;
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

    public int getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(int stopOrder) {
        if (stopOrder < 1) {
            throw new IllegalArgumentException("Stop order must be 1 or higher.");
        }
        this.stopOrder = stopOrder;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        if (Validator.isBlank(stopName)) {
            throw new IllegalArgumentException("Stop name must not be blank.");
        }
        this.stopName = stopName.trim();
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        if (Validator.isBlank(road)) {
            throw new IllegalArgumentException("Road must not be blank.");
        }
        this.road = road.trim();
    }
}
