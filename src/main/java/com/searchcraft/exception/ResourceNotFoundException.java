package com.searchcraft.exception;

/**
 * Thrown when a requested resource (e.g. IndexedPage with given ID) does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
