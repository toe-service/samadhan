package com.samadhan.exception;

// A driver (agent) with this driver_contact_number already exists — see driversServiceImpl#createdriver.
public class DuplicateAgentNumberException extends RuntimeException {

    public DuplicateAgentNumberException(String message) {
        super(message);
    }
}
