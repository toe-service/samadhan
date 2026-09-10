package com.samadhan.exception;

// The vehicle a vendor is trying to assign is outside the allowed distance from the ride's
// pickup point (see TransferRequestServiceImpl#requestTransferApproval). Distinct from a
// generic RuntimeException so GlobalExceptionHandler can return a specific, actionable
// identifier ("VehicleTooFar") instead of a generic 500 "Server" error.
public class VehicleTooFarException extends RuntimeException {

    public VehicleTooFarException(String message) {
        super(message);
    }
}
