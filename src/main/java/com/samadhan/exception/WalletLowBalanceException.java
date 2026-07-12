package com.samadhan.exception;


public class WalletLowBalanceException extends RuntimeException {

    public WalletLowBalanceException(String message) {
        super(message);
    }
}
