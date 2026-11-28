package com.byahero.exception;

/** Our own exception for when the same bus is already inside the terminal. */
public class DuplicateBusException extends RuntimeException {

    public DuplicateBusException(String message) {
        super(message);
    }
}
