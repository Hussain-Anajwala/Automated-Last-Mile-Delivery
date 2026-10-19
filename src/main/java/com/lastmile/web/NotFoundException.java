package com.lastmile.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Record does not exist or is outside the caller's scope. Both cases produce the same 404 so an
 * agent cannot probe for other agents' deliveries.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
