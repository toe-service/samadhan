package com.samadhan.exception;

public class SubscriptionSuspendedException extends RuntimeException {

    public SubscriptionSuspendedException(String message) {
        super(message);
    }
}
