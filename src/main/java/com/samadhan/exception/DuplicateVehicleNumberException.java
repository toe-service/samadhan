package com.samadhan.exception;

// A vehicle with this vehicle_number already exists — see VehicleServiceImpl#createVehicle.
public class DuplicateVehicleNumberException extends RuntimeException {

    public DuplicateVehicleNumberException(String message) {
        super(message);
    }
}
