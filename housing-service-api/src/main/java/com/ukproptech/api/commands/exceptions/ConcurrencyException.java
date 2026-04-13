package com.ukproptech.api.commands.exceptions;

/**
 * Exception thrown when several users try to update the same aggregate simultaneously.
 */
public class ConcurrencyException extends RuntimeException {
    public ConcurrencyException(String message) {
        super(message);
    }
}
