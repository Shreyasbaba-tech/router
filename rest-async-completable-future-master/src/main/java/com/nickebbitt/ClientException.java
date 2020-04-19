package com.nickebbitt;

import org.springframework.http.HttpStatus;

public class ClientException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HttpStatus httpStatus;
	private String errorResponse;

	public String getErrorResponse() {
		return errorResponse;
	}

	public void setErrorResponse(String errorResponse) {
		this.errorResponse = errorResponse;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	public ClientException(HttpStatus badRequest, String errorResponse) {
		this.httpStatus = badRequest;
		this.errorResponse = errorResponse;
	}
}