package com.bughunt.exception;

public class InCompleteSettingsException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public InCompleteSettingsException(String message) {
		super(message);
	}

}
