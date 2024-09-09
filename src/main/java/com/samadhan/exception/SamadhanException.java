package com.samadhan.exception;

public class SamadhanException extends Exception {

	private static final long serialVersionUID = 1L;

	public SamadhanException(String message) {
		super(message);
	}

	public SamadhanException(Throwable cause) {
		super(cause);
	}

	public SamadhanException(String message, Throwable cause) {
		super(message, cause);
	}
}

