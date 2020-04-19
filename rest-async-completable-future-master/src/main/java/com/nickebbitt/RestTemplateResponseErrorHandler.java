package com.nickebbitt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

@Component
public class RestTemplateResponseErrorHandler 
  implements ResponseErrorHandler {
	
	private final Logger logger = LoggerFactory.getLogger(RestTemplateResponseErrorHandler.class);
 
    @Override
    public boolean hasError(ClientHttpResponse httpResponse) 
      throws IOException {
 
    	 return (httpResponse
    	          .getStatusCode()
    	          .series() == HttpStatus.Series.CLIENT_ERROR || httpResponse
    	          .getStatusCode()
    	          .series() == HttpStatus.Series.SERVER_ERROR);
    }
 
    @Override
    public void handleError(ClientHttpResponse httpResponse) 
      throws IOException {
 
        if (httpResponse.getStatusCode()
          .series() == HttpStatus.Series.SERVER_ERROR) {
            // handle SERVER_ERROR
        } else if (httpResponse.getStatusCode()
          .series() == HttpStatus.Series.CLIENT_ERROR) {
            // handle CLIENT_ERROR
            if (httpResponse.getStatusCode() == HttpStatus.BAD_REQUEST || httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ClientException(httpResponse.getStatusCode(),readErrorResponse(httpResponse));
            }
            if (httpResponse.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
                throw new ClientException(HttpStatus.GATEWAY_TIMEOUT,readErrorResponse(httpResponse));
            }
        }
    }
    
    private String readErrorResponse(ClientHttpResponse response) {
    	StringBuilder inputStringBuilder = new StringBuilder();
    	try {
    		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
    		String line = bufferedReader.readLine();
    		while (line != null) {
    			inputStringBuilder.append(line);
    			inputStringBuilder.append('\n');
    			line = bufferedReader.readLine();
    		}
    	}catch(Exception ex) {
    		logger.error(ex.getMessage());
    	}
    	return inputStringBuilder.toString();
    }
}