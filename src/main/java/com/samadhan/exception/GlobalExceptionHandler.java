package com.samadhan.exception;

import com.samadhan.response.Error;
import com.samadhan.util.ResponseUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

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



    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(ConflictException ex) {
        return ResponseEntity.badRequest().body(ResponseUtil.populateResponseObject(
                null,
                "409",
                new Error("Database", "Duplicate or invalid value: " + ex.getMessage())
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
    
    @ExceptionHandler(SubscriptionSuspendedException.class)
    public ResponseEntity<Object> handleSubscriptionSuspended(
            SubscriptionSuspendedException ex) {

    	 return ResponseEntity.status(403).body(
                 ResponseUtil.populateResponseObject(
                         null,
                         "403",
                         new Error("Subscription", ex.getMessage())
                 )
         );
    }
    
    @ExceptionHandler(WalletLowBalanceException.class)
    public ResponseEntity<Object> handleSubscriptionSuspended(
    		WalletLowBalanceException ex) {

    	 return ResponseEntity.status(403).body(
                 ResponseUtil.populateResponseObject(
                         null,
                         "403",
                         new Error("Wallet", ex.getMessage())
                 )
         );
    }

    // The vendor picked a vehicle that's too far from the pickup point to be assigned to this
    // ride — a specific, expected rejection (not a server fault), so the client can show
    // "pick a closer vehicle" instead of a generic error.
    @ExceptionHandler(VehicleTooFarException.class)
    public ResponseEntity<Object> handleVehicleTooFar(VehicleTooFarException ex) {
        return ResponseEntity.status(409).body(
                ResponseUtil.populateResponseObject(
                        null,
                        "409",
                        new Error("VehicleTooFar", ex.getMessage())
                )
        );
    }

    // Someone else already accepted/claimed this request before this call landed — a normal
    // race in a broadcast-then-first-accept model, not a server fault.
    @ExceptionHandler(RequestAlreadyAcceptedException.class)
    public ResponseEntity<Object> handleRequestAlreadyAccepted(RequestAlreadyAcceptedException ex) {
        return ResponseEntity.status(409).body(
                ResponseUtil.populateResponseObject(
                        null,
                        "409",
                        new Error("AlreadyAccepted", ex.getMessage())
                )
        );
    }

    // Bad/missing input (e.g. "Parcel Type is required.", "New password must be at least 6
    // characters long") — a client mistake, not a server fault, so it belongs on 400 with the
    // original message rather than falling through to the generic 500 handler below.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                ResponseUtil.populateResponseObject(
                        null,
                        "400",
                        new Error("Validation", ex.getMessage())
                )
        );
    }

    // Catch-all for any other "the current state doesn't allow this action" business-rule
    // rejection that doesn't yet have its own specific exception type — still not a server
    // fault, so it gets 409 with the original message instead of a generic 500.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(409).body(
                ResponseUtil.populateResponseObject(
                        null,
                        "409",
                        new Error("State", ex.getMessage())
                )
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> notFoundException(NotFoundException ex) {
        return ResponseEntity.status(404).body(
                ResponseUtil.populateResponseObject(
                        null,
                        "404",
                        new Error("NotFound", ex.getMessage())
                )
        );
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<Object> handleRefreshTokenException(RefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ResponseUtil.populateResponseObject(
                        null,
                        "401",
                        new Error("Auth", ex.getMessage())
                )
        );
    }

    @ExceptionHandler(OtpMismatchException.class)
    public ResponseEntity<Object> handleOtpMismatchException(OtpMismatchException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ResponseUtil.populateResponseObject(
                        null,
                        "401",
                        new Error("Server", "Unexpected error: " + ex.getMessage())
                )
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ResponseUtil.populateResponseObject(
                        null,
                        "401",
                        new Error("Auth", ex.getMessage())
                )
        );
    }

    // Thrown when a request's own JWT identity doesn't match the resource it's trying to act on
    // (e.g. changing another vendor's password) — see TransferVendorController#changePassword.
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ResponseUtil.populateResponseObject(
                        null,
                        "403",
                        new Error("Auth", ex.getMessage())
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
    
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
    
}
