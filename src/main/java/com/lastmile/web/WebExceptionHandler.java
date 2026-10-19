package com.lastmile.web;

import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Browser pages: a concurrent update is a 409 conflict page, not a 500. */
@ControllerAdvice(basePackages = {"com.lastmile.delivery", "com.lastmile.dashboard", "com.lastmile.user", "com.lastmile.web"})
public class WebExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String concurrentUpdate() {
        return "error/409";
    }
}
