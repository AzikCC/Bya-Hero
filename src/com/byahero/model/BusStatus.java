package com.byahero.model;

/**
 * The three steps in a bus's life at the terminal.
 * A bus can only go forward one step at a time: ARRIVED -> LOADING -> DEPARTED.
 */
public enum BusStatus {
    ARRIVED,
    LOADING,
    DEPARTED;

    /** The only status allowed after this one. DEPARTED has no next status (null). */
    public BusStatus next() {
        if (this == ARRIVED) {
            return LOADING;
        }
        if (this == LOADING) {
            return DEPARTED;
        }
        return null;
    }

    /** True only if "target" is exactly the next step. Skips and going backward return false. */
    public boolean canMoveTo(BusStatus target) {
        BusStatus allowed = next();
        if (allowed == null) {
            return false;
        }
        return allowed == target;
    }

    /** Converts text like "loading" into BusStatus.LOADING. */
    public static BusStatus fromString(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Status must not be blank.");
        }
        try {
            return BusStatus.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status: " + text);
        }
    }
}
