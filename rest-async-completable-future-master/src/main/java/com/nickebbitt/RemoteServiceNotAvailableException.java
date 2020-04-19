package com.nickebbitt;

public class RemoteServiceNotAvailableException extends RuntimeException{
	
	private String exceptionMsg;

	public String getExceptionMsg() {
		return exceptionMsg;
	}

	public void setExceptionMsg(String exceptionMsg) {
		this.exceptionMsg = exceptionMsg;
	}

	public RemoteServiceNotAvailableException(String message) {
		this.exceptionMsg = message;
	}

}
