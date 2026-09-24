package com.samadhan.exception;

// Vendor tried to register another vehicle past their plan's fleet-size limit (see
// VehicleServiceImpl#createVehicle) — an individual account capped at 1, or a non-subscribing
// vendor capped at 3. Distinct from a generic RuntimeException so GlobalExceptionHandler can
// return a specific, actionable identifier ("VehicleLimit") instead of a generic 500 "Server"
// error or ConflictException's misleading "Duplicate or invalid value:" prefix.
public class VehicleLimitExceededException extends RuntimeException {

    public VehicleLimitExceededException(String message) {
        super(message);
    }
}
