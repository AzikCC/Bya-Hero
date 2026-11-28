package com.byahero.exception;

/** Our own exception for when a CSV file cannot be read or written. */
public class CsvStorageException extends RuntimeException {

    public CsvStorageException(String message) {
        super(message);
    }

    public CsvStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
