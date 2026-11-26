package com.byahero.model;

/** INHERITANCE: ProvincialBus is a Bus. It adds a fixed long-haul terminal fee. */
public class ProvincialBus extends Bus {

    /** The name saved in buses.csv and used by BusFactory. */
    public static final String TYPE = "PROVINCIAL";

    private static final double TERMINAL_FEE = 50.0;

    public ProvincialBus(String plate, String operator, String destination, BusStatus status,
                         String bay, String checkInTime, String estDeparture) {
        super(plate, operator, destination, status, bay, checkInTime, estDeparture);
    }

    /** Example: base fare 100 -> 100 + 50 = 150 */
    @Override
    public double calculateFare(double baseFare) {
        checkBaseFare(baseFare);
        return baseFare + TERMINAL_FEE;
    }

    @Override
    public String getBusType() {
        return TYPE;
    }
}
