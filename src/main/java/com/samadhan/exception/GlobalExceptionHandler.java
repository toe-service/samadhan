package com.samadhan.exception;

import com.samadhan.response.Error;
import com.samadhan.util.ResponseUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // For duplicate key / unique constraint violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.badRequest().body(ResponseUtil.populateResponseObject(
                null,
                "400",
                new Error("Database", "Duplicate or invalid value: " + ex.getMostSpecificCause().getMessage())
        ));
    }

    // For your custom exceptions
    @ExceptionHandler(SamadhanException.class)
    public ResponseEntity<Object> handleSamadhanException(SamadhanException ex) {
        return ResponseEntity.internalServerError().body(
                ResponseUtil.populateResponseObject(
                        null,
                        "500",
                        new Error("RideStartEnd", ex.getMessage())
                )
        );
    }

    // Generic fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        return ResponseEntity.internalServerError().body(
                ResponseUtil.populateResponseObject(
                        null,
                        "500",
                        new Error("Server", "Unexpected error: " + ex.getMessage())
                )
        );
    }
}
