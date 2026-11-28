package com.byahero.model;

import com.byahero.exception.InvalidPlateException;
import com.byahero.util.Validator;

/**
 * ABSTRACTION: Bus is an abstract class, so we never create a plain Bus.
 * We create an OrdinaryBus or a ProvincialBus instead.
 * Each child class has its own way to calculate the fare.
 *
 * ENCAPSULATION: all fields are private. Other classes must use the
 * getters and setters, and the setters check that the value is valid.
 */
public abstract class Bus {

    // Plate rule: 3 capital letters, a dash, then 3 or 4 digits (example: ABC-1234)
    private static final String PLATE_REGEX = "^[A-Z]{3}-\\d{3,4}$";

    private String plate;
    private String operator;
    private String destination;
    private BusStatus status;
    private String bay;
    private String checkInTime;
    private String estDeparture;

    public Bus(String plate, String operator, String destination, BusStatus status,
               String bay, String checkInTime, String estDeparture) {
        // We use the setters here so the validation also runs when a bus is created
        setPlate(plate);
        setOperator(operator);
        setDestination(destination);
        setStatus(status);
        setBay(bay);
        setCheckInTime(checkInTime);
        setEstDeparture(estDeparture);
    }

    /** Every bus type must write its own fare formula. */
    public abstract double calculateFare(double baseFare);

    /** Every bus type must say its name (ORDINARY or PROVINCIAL). Used in the CSV file. */
    public abstract String getBusType();

    // ---------- helper methods ----------

    /** Returns true if the plate matches the rule, like ABC-123. */
    public static boolean isValidPlate(String plate) {
        if (plate == null) {
            return false;
        }
        return plate.matches(PLATE_REGEX);
    }

    /** A bus is "active" while it is still in the terminal (not DEPARTED yet). */
    public boolean isActive() {
        return status != BusStatus.DEPARTED;
    }

    /** Moves the bus to its next status. Throws an error if the move is not allowed. */
    public void changeStatus(BusStatus newStatus) {
        if (!status.canMoveTo(newStatus)) {
            throw new IllegalArgumentException("Invalid status change: " + status + " -> " + newStatus
                    + ". The order must be ARRIVED -> LOADING -> DEPARTED.");
        }
        this.status = newStatus;
    }

    /** The child classes call this to make sure the base fare is not negative. */
    protected void checkBaseFare(double baseFare) {
        if (baseFare < 0) {
            throw new IllegalArgumentException("Base fare cannot be negative.");
        }
    }

    // ---------- getters and setters ----------

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        if (plate == null) {
            throw new InvalidPlateException("Plate must not be blank.");
        }
        String cleaned = plate.trim().toUpperCase();
        if (!isValidPlate(cleaned)) {
            throw new InvalidPlateException("Invalid plate '" + plate + "'. Use a format like ABC-123 or ABC-1234.");
        }
        this.plate = cleaned;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        if (Validator.isBlank(operator)) {
            throw new IllegalArgumentException("Operator must not be blank.");
        }
        this.operator = operator.trim();
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

    public BusStatus getStatus() {
        return status;
    }

    public void setStatus(BusStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status must not be empty.");
        }
        this.status = status;
    }

    public String getBay() {
        return bay;
    }

    public void setBay(String bay) {
        if (Validator.isBlank(bay)) {
            throw new IllegalArgumentException("Bay must not be blank.");
        }
        this.bay = bay.trim();
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        if (Validator.isBlank(checkInTime)) {
            throw new IllegalArgumentException("Check-in time must not be blank.");
        }
        this.checkInTime = checkInTime.trim();
    }

    public String getEstDeparture() {
        return estDeparture;
    }

    public void setEstDeparture(String estDeparture) {
        if (Validator.isBlank(estDeparture)) {
            throw new IllegalArgumentException("Estimated departure must not be blank.");
        }
        this.estDeparture = estDeparture.trim();
    }
}
