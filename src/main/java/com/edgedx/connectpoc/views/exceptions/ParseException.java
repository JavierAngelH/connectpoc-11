package com.edgedx.connectpoc.views.exceptions;

public class ParseException extends Exception {

	private String message;

	@Override
	public String getMessage() {
		return message;
	}

	public ParseException(String message) {
		this.message = message;
		
	}

}
