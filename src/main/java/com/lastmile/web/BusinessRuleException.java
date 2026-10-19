package com.lastmile.web;

/**
 * Input is well-formed but violates a business rule (HTTP 400). {@code field} names the form
 * field to attach the message to, or is null for a form-level error.
 */
public class BusinessRuleException extends RuntimeException {

    private final String field;

    public BusinessRuleException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
