package com.nickebbitt;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableRetry
@EnableWebFlux
@RestController
@RequestMapping("/rxmicro/services")
public class RxMicroAPIGatewayController {

	private final Logger logger = LoggerFactory.getLogger(RxMicroAPIGatewayController.class);
	
	@Autowired
	private GatewayRouteProperties gatewayRouteProperties;

	@Autowired
	private Environment environmentArgs;
	
	@Autowired
	AsyncService asyncService;
	
	private static HashMap<String, String> uriMap = new HashMap<String, String>();
	
//	ExecutorService executorService = Executors.newCachedThreadPool();
	
	/*
	 * @Bean public RestTemplate restTemplate() { RestTemplate restTemplate = new
	 * RestTemplate(); restTemplate.setErrorHandler(new
	 * RestTemplateResponseErrorHandler()); return restTemplate; }
	 */

	
	public static void main(String[] args) {
		SpringApplication.run(RxMicroAPIGatewayController.class, args);
	}
	
	@GetMapping(path = "/{requestUri}", produces = MediaType.APPLICATION_JSON_VALUE) 
	public  Object getValueAsyncUsingCompletableFuture(@PathVariable("requestUri") String requestUri, String request,
			@RequestParam MultiValueMap<String, String> allQueryParams, @RequestHeader Map<String, String> allrequestHeaders,
			HttpServletRequest servletRequest) {

		return intiateHTTPGetRequest(requestUri, request, allQueryParams, allrequestHeaders, servletRequest);

	}
	
	@GetMapping(path = "{requestPath}/{requestUri}", produces = MediaType.APPLICATION_JSON_VALUE) 
	public  Object getModemShipments(@PathVariable("requestPath") String requestPath , @PathVariable("requestUri") String requestUri, String request,
			@RequestParam MultiValueMap<String, String> allQueryParams, @RequestHeader Map<String, String> allrequestHeaders,
			HttpServletRequest servletRequest) {

		return intiateHTTPGetRequest(requestPath+"/"+requestUri, request, allQueryParams, allrequestHeaders, servletRequest);

	}


	@PostMapping(path = "/{requestUri}", consumes = {MediaType.APPLICATION_XML_VALUE,MediaType.TEXT_XML_VALUE}, produces =MediaType.APPLICATION_XML_VALUE) 
	public Object postAsyncUsingCompletableFuture(@PathVariable("requestUri") String requestUri, String request,
			@RequestParam Map<String, String> allQueryParams, @RequestHeader Map<String, String> allrequestHeaders,
			HttpServletRequest servletRequest) {

		return intiateHTTPPostRequest(requestUri, request, allQueryParams, allrequestHeaders, servletRequest);

	}


	@PostMapping(path = "/{requestUri}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Object postJSONRequest(@PathVariable("requestUri") String requestUri, String request,
			@RequestParam Map<String, String> allQueryParams, @RequestHeader Map<String, String> allrequestHeaders,
			HttpServletRequest servletRequest) {

		return intiateHTTPPostRequest(requestUri, request, allQueryParams, allrequestHeaders, servletRequest);
	}
	
	@PostMapping(path = "{requestPath}/{requestUri}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Object processRealTimePO(@PathVariable("requestPath") String requestPath , @PathVariable("requestUri") String requestUri, String request,
			@RequestParam Map<String, String> allQueryParams, @RequestHeader Map<String, String> allrequestHeaders,
			HttpServletRequest servletRequest) {

		return intiateHTTPPostRequest(requestPath+"/"+requestUri, request, allQueryParams, allrequestHeaders, servletRequest);
	}

	public Object intiateHTTPGetRequest(String requestUri, String request,
			MultiValueMap<String, String> allQueryParams, Map<String, String> allrequestHeaders,
			HttpServletRequest servletRequest){
		
		logger.info(servletRequest.getMethod() + " Request received for " +requestUri);

		allQueryParams.forEach((k, v) -> logger.info("Request Param Key : " + k + " Request Param Value : " + v));
		
		String URI = prepareRedirectRequestURI(servletRequest, requestUri);
		
		Object completableFuture = asyncService.processGetRequest(URI, allQueryParams, allrequestHeaders);
		
				  logger.info("Servlet thread released");

		return completableFuture;
		
	}
	
	private Object intiateHTTPPostRequest(String requestUri, String request,
			Map<String, String> allQueryParams, Map<String, String> allrequestHeaders,
			HttpServletRequest servletRequest){

		logger.info(servletRequest.getMethod() + " Request received for "+requestUri);
		
		long inTime = System.currentTimeMillis();
		
		allrequestHeaders.forEach((k, v) -> logger.info("Request Param Key : " + k + " Request Param Value : " + v));

		String requestBody = readRequestBody(servletRequest);
		
		logTimeTaken(inTime, "readRequestBody "+requestUri);

		logger.info("Request :: " + requestBody);

		String URI = prepareRedirectRequestURI(servletRequest, requestUri);
		
		Object completableFuture = asyncService.processPostRequest(URI, requestBody, allrequestHeaders);
		
//				logger.info("Servlet thread released");

		return completableFuture;
	}


	private String prepareRedirectRequestURI(HttpServletRequest servletRequest, String pathURI) {
		
		if(!StringUtils.isEmpty(uriMap.get(servletRequest.getRequestURI()))) {
			return uriMap.get(servletRequest.getRequestURI());
		}

		Map<String, GatewayRouteProperties.Route> routeMap = gatewayRouteProperties.getRoutes();

		for (String name : routeMap.keySet()) {
			GatewayRouteProperties.Route route = routeMap.get(name);
			if (route.getPredicates().stream()
					.filter(s -> Pattern.compile(s).matcher(servletRequest.getRequestURI()).find()).findFirst()
					.isPresent()) {
				//				return route.getHost() + route.getPath() + pathURI;
				uriMap.put(servletRequest.getRequestURI(), environmentArgs.getProperty(route.getHost())+ route.getPath() + pathURI);
				return environmentArgs.getProperty(route.getHost())+ route.getPath() + pathURI;
			}
		}
		return "";
	}
	
	private void logTimeTaken(long inTime,String requestUri) {
		long timeTaken = System.currentTimeMillis() - inTime;

		logger.info("Time taken for "+ requestUri+ " :: "+ timeTaken+"ms");
	}


	private String readRequestBody(HttpServletRequest servletRequest) {
		String request = "";

		try {
			request = servletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (Exception e) {
			logger.info("Could not get the request body");
		}

		return request;
	}

}
