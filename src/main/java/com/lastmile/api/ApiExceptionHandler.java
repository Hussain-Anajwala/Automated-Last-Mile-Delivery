package com.lastmile.api;

import java.util.List;

import com.lastmile.api.ApiDtos.ErrorResponse;
import com.lastmile.api.ApiDtos.FieldError;
import com.lastmile.web.BusinessRuleException;
import com.lastmile.web.ConflictException;
import com.lastmile.web.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Maps failures to the error contract in docs/Backend_Schema.md section 9. */
@RestControllerAdvice(assignableTypes = DeliveryApiController.class)
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> invalid(MethodArgumentNotValidException ex) {
        List<FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
        return body(HttpStatus.BAD_REQUEST, "Validation failed.", fields);
    }

    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<ErrorResponse> businessRule(BusinessRuleException ex) {
        List<FieldError> fields = ex.getField() == null ? List.of() : List.of(new FieldError(ex.getField(), ex.getMessage()));
        return body(HttpStatus.BAD_REQUEST, ex.getMessage(), fields);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ErrorResponse> malformed(Exception ex) {
        return body(HttpStatus.BAD_REQUEST, "Malformed request.", List.of());
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(NotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, "Delivery not found.", List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> forbidden(AccessDeniedException ex) {
        return body(HttpStatus.FORBIDDEN, "You do not have permission to do this.", List.of());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ErrorResponse> conflict(ConflictException ex) {
        return body(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ErrorResponse> concurrentUpdate(ObjectOptimisticLockingFailureException ex) {
        return body(HttpStatus.CONFLICT, "This delivery was changed by someone else. Reload and try again.", List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception ex) {
        log.error("Unexpected API error", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error.", List.of());
    }

    private static ResponseEntity<ErrorResponse> body(HttpStatus status, String message, List<FieldError> fields) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message, fields));
    }
}
