package com.nickebbitt;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rxmicro.gateway")
public class GatewayRouteProperties {
	
	private String uri;
	
	 private List<String> headers;
	 
	 private Integer connectionTimeout;

    private Map<String, Route> routes = new LinkedHashMap<String, Route>();
    
    private HttpConnectionPool httpConnectionPool;

    public Integer getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(Integer connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public HttpConnectionPool getHttpConnectionPool() {
		return httpConnectionPool;
	}

	public void setHttpConnectionPool(HttpConnectionPool httpConnectionPool) {
		this.httpConnectionPool = httpConnectionPool;
	}

	public Map<String, Route> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, Route> routes) {
        this.routes = routes;
    }
    

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}


	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}


	public static class Route {
        private String host;
        
        private String path;
//        private String predicate;
        
        private List<String> predicates;
        
        

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public List<String> getPredicates() {
			return predicates;
		}

		public void setPredicates(List<String> predicates) {
			this.predicates = predicates;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

        
    }
	
	public static class HttpConnectionPool {
		
		private Integer maxTotal;
		private Integer defaultMaxPerRoute;
		public Integer getMaxTotal() {
			return maxTotal;
		}
		public void setMaxTotal(Integer maxTotal) {
			this.maxTotal = maxTotal;
		}
		public Integer getDefaultMaxPerRoute() {
			return defaultMaxPerRoute;
		}
		public void setDefaultMaxPerRoute(Integer defaultMaxPerRoute) {
			this.defaultMaxPerRoute = defaultMaxPerRoute;
		}
		
	}

}