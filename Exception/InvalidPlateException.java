package com.byahero.exception;

/**
 * Our own exception for a wrong plate number.
 * Example of a good plate: ABC-123 or ABC-1234.
 */
public class InvalidPlateException extends RuntimeException {

    public InvalidPlateException(String message) {
        super(message);
    }
}
