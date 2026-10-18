package com.samadhan.exception;

// A vendor with this vendor_name (used to derive the login username) already exists —
// see TransferVendorServiceImpl#registerVendor.
public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String message) {
        super(message);
    }
}
