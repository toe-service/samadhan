package com.samadhan.exception;

// Someone else already accepted this transfer request before the current caller's
// accept/decline call landed (see TransferRequestServiceImpl#requestTransferApproval).
// Distinct from a generic RuntimeException so GlobalExceptionHandler can return a specific,
// actionable identifier ("AlreadyAccepted") instead of a generic 500 "Server" error.
public class RequestAlreadyAcceptedException extends RuntimeException {

    public RequestAlreadyAcceptedException(String message) {
        super(message);
    }
}
