package com.nickebbitt;

import java.util.Map;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.MultiValueMap;


public interface AsyncService {
	
	 @Retryable(value = {RemoteServiceNotAvailableException.class},maxAttempts = 3, backoff = @Backoff(2000))
	public Object processGetRequest(String URI,MultiValueMap<String, String> allQueryParams,
    		Map<String, String> allrequestHeaders);
	
	 @Retryable(value = {RemoteServiceNotAvailableException.class},maxAttempts = 3, backoff = @Backoff(2000))
	public Object processPostRequest(String URI ,String requestBody, Map<String, String> allrequestHeaders );
	
	@Recover
	public Object getBackendResponseFallback(RemoteServiceNotAvailableException notAvailableException);

}
