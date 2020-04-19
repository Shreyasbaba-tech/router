package com.nickebbitt;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;
 
@Service
public class AsyncServiceImpl implements AsyncService{
 
    private final Logger logger = LoggerFactory.getLogger(RxMicroAPIGatewayController.class);
    
    @Autowired
	private GatewayRouteProperties gatewayRouteProperties;
 
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
	private ApiResponse apiResponse;
    
    @Autowired
    private CloseableHttpClient httpclient;
    
    private static WebClient webClient = WebClient.create();
  
	@Override
	public Object processGetRequest(String URI, MultiValueMap<String, String> allQueryParams,
			Map<String, String> allrequestHeaders) {
		
		long inTime = System.currentTimeMillis();
		
    	try {
    		
			URI uri = UriComponentsBuilder.fromUriString(URI).queryParams(allQueryParams).build().toUri();

			logger.info("Redirecting Request to :: " + uri.toString());

			HttpHeaders headers = new HttpHeaders();

			headers.setAll(addHeadersAttribute(allrequestHeaders));
			
			logTimeTaken(inTime, "addHeadersAttribute");

			HttpEntity<String> requestEntiry = new HttpEntity<String>(headers);
			
			
			
			Mono<String> result = webClient.get().uri(uri).retrieve()
                    .bodyToMono(String.class);
			String response = result.block();

//			ResponseEntity<String> responseEntiry  = restTemplate.exchange(uri, HttpMethod.GET, requestEntiry, String.class);
			
//			ResponseEntity<String> responseEntiry  = restTemplate.getForEntity(uri, String.class);
			
			
			/*
			 * HttpUriRequest request = new HttpGet(uri);
			 * 
			 * HttpResponse httpResponse = httpclient.execute(request);
			 * 
			 * String response = EntityUtils.toString(httpResponse.getEntity(),
			 * Charset.forName("UTF-8"));
			 */
			 
			logTimeTaken(inTime, uri.toString());

//			return responseEntiry.getBody();
			return response;
		}catch(ClientException ex) {
			logger.error("Client Error while fetching the response for "+ URI +" :: "+ex.getHttpStatus().toString());
			return ResponseEntity.status(ex.getHttpStatus()).body(StringUtils.hasText(ex.getErrorResponse())? ex.getErrorResponse() : 
				throwAPIErrorResponse("Internal Error"));
		}catch (IllegalArgumentException ex) {
			logger.error("IllegalArgumentException : API not configured in micro gateway"+ URI +" :: "+ex.getMessage());
		}catch(ResourceAccessException ex) {
			logger.error("ResourceAccessException resubmitting the request ::"+ex.getMessage());
			logTimeTaken(inTime, URI);
			throw new RemoteServiceNotAvailableException(ex.getMessage());
		}catch(Exception e) {
			logger.error("Error while fetching the response for "+ URI +" :: "+e.getMessage());
			if(!StringUtils.isEmpty(e.getMessage()) && (e.getMessage().contains("I/O error") || e.getMessage().contains("404"))) {
				logger.error("Exception resubmitting the request ::"+e.getMessage());
				throw new RemoteServiceNotAvailableException(e.getMessage());
			}
		}
    	logTimeTaken(inTime, URI);
        return throwAPIErrorResponse("Internal Error");
	}

	@Override
	public Object processPostRequest(String URI ,String requestBody, Map<String, String> allrequestHeaders ) {
		
		long inTime = System.currentTimeMillis();
		
		try {
			
			logger.info("Redirecting Request to :: " + URI);

			ResponseEntity<String> responseEntiry = postRequest(requestBody, URI, allrequestHeaders);
			
			logTimeTaken(inTime, URI);
			
			return CompletableFuture.completedFuture(responseEntiry.getBody());
		}catch(ClientException ex) {
			logger.error("Client Error while fetching the response for "+ URI +" :: "+ex.getHttpStatus().toString());
			return CompletableFuture.completedFuture(ResponseEntity.status(ex.getHttpStatus()).body(StringUtils.hasText(ex.getErrorResponse())? ex.getErrorResponse() : 
				throwAPIErrorResponse("Internal Error")));
		}catch (IllegalArgumentException ex) {
			logger.error("IllegalArgumentException : API not configured in micro gateway"+ URI +" :: "+ex.getMessage());
		}catch(ResourceAccessException ex) {
			logger.error("ResourceAccessException resubmitting the request ::"+ex.getMessage());
			logTimeTaken(inTime, URI);
			throw new RemoteServiceNotAvailableException(ex.getMessage());
		}catch(Exception e) {
			logger.error("Error while fetching the response for "+ URI +" :: "+e.getMessage());
			if(!StringUtils.isEmpty(e.getMessage()) && (e.getMessage().contains("I/O error") || e.getMessage().contains("404"))) {
				logger.error("Exception resubmitting the request ::"+e.getMessage());
				throw new RemoteServiceNotAvailableException(e.getMessage());
			}
		}
		logTimeTaken(inTime, URI);
        return CompletableFuture.completedFuture(throwAPIErrorResponse("Internal Error"));
	}
	
	private ResponseEntity<String> postRequest(String requestBody,String URI,Map<String, String> allrequestHeaders){

		HttpHeaders headers = new HttpHeaders();

		headers.setAll(addHeadersAttribute(allrequestHeaders));

		HttpEntity<String> requestEntiry = new HttpEntity<String>(requestBody, headers);

		ResponseEntity<String> responseEntiry = restTemplate.postForEntity(URI, requestEntiry, String.class);

		return responseEntiry;
	}
	
	private Map<String,String> addHeadersAttribute(Map<String, String> allrequestHeaders){
		Map<String, String> collectHeaders = allrequestHeaders.entrySet().stream()
				.filter(x -> gatewayRouteProperties.getHeaders().contains(x.getKey().toLowerCase()))
				.collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
		
		collectHeaders.put("connection", "close");
		
		collectHeaders.forEach((k, v) -> logger.info("Header Param Key : " + k + ", Header Param Value : " + v));

		return collectHeaders;
	}
	
	private void logTimeTaken(long inTime,String requestUri) {
		long timeTaken = System.currentTimeMillis() - inTime;

		logger.info("Time taken for "+ requestUri+ " :: "+ timeTaken+"ms");
	}
 
    
	private ApiResponse throwAPIErrorResponse(String httErrorMessage) {
		apiResponse.setMessage(httErrorMessage);
		apiResponse.setCode(02);
		apiResponse.setType("Failure");
		return apiResponse;
	}

	@Override
	public Object getBackendResponseFallback(RemoteServiceNotAvailableException notAvailableException) {
		logger.info("Recovered from retry ::"+notAvailableException.getExceptionMsg());
		return CompletableFuture.completedFuture(ResponseEntity.status(503).body(throwAPIErrorResponse("Internal Gateway Error")));
	}

}