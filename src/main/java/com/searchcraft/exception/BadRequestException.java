package com.searchcraft.exception;

/**
 * Thrown when an incoming request contains invalid or malicious parameters.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
