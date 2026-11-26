package com.byahero.model;

/** INHERITANCE: OrdinaryBus is a Bus. It pays just the base fare. */
public class OrdinaryBus extends Bus {

    /** The name saved in buses.csv and used by BusFactory. */
    public static final String TYPE = "ORDINARY";

    public OrdinaryBus(String plate, String operator, String destination, BusStatus status,
                       String bay, String checkInTime, String estDeparture) {
        super(plate, operator, destination, status, bay, checkInTime, estDeparture);
    }

    /** POLYMORPHISM: same method name as in Bus, but its own formula. */
    @Override
    public double calculateFare(double baseFare) {
        checkBaseFare(baseFare);
        return baseFare;
    }

    @Override
    public String getBusType() {
        return TYPE;
    }
}
