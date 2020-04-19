package com.nickebbitt;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


@Configuration
public class RestTemplateConfig {
	
	@Bean
	public RestTemplate restTemplate( ) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
	    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory( );
	    requestFactory.setHttpClient(getHttpClient() );
	    RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory( requestFactory ) );
	    restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
//	    restTemplate.setInterceptors( Collections.singletonList( new LoggingRequestInterceptor( ) ) );
//	    restTemplate.setMessageConverters( this.getMessageConverters( ) );
	    return restTemplate;
	 }
	
	@Bean
	public CloseableHttpClient getHttpClient( ) throws NoSuchAlgorithmException, KeyManagementException {
		SSLConnectionSocketFactory socketFactory = getSocketFactory( );
		
		return HttpClientBuilder.create( )
								.setDefaultRequestConfig( getRequestConfig( ) )
								.setConnectionManager( getConnectionManager( socketFactory ) )
								.setSSLSocketFactory( socketFactory )
								.build( );
	}
	
	private List<HttpMessageConverter<?>> getMessageConverters( ) {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();
		FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMessageConverter.setObjectMapper(objectMapper);
		messageConverters.add(jsonMessageConverter);
		messageConverters.add(formHttpMessageConverter);

		return messageConverters;
		
	}
	
	
	
	
	private RequestConfig getRequestConfig( ) {
		return RequestConfig.custom( )
							.setConnectTimeout( 180000 )
							.setSocketTimeout( 180000 )
							.build( );
	}
	
	private PoolingHttpClientConnectionManager getConnectionManager( SSLConnectionSocketFactory socketFactory ) {
		PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager( getRegistry( socketFactory ) );
		manager.setMaxTotal( 200 );
		manager.setDefaultMaxPerRoute( 200 );
		return manager;
	}
	
	
	private Registry<ConnectionSocketFactory> getRegistry( SSLConnectionSocketFactory socketFactory ) {
		return RegistryBuilder.<ConnectionSocketFactory>create( )
							  .register( "http", PlainConnectionSocketFactory.getSocketFactory( ) )
							  .register( "https", socketFactory )
							  .build( );
	}
	
    private SSLConnectionSocketFactory getSocketFactory( ) throws KeyManagementException, NoSuchAlgorithmException {
    	SSLContext sslContext = SSLContext.getDefault();
//        sslContext.init( null, new TrustManager[]{ getTrustAllManager( ) }, new SecureRandom( ) );
        return new SSLConnectionSocketFactory( sslContext);
    }

    private X509TrustManager getTrustAllManager( ) {
        return new X509TrustManager( ) {
            @Override
            public void checkClientTrusted( X509Certificate[] cert, String s ) throws CertificateException {
            }

            @Override
            public void checkServerTrusted( X509Certificate[] cert, String s ) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers( ) {
                return null;
            }
        };
    }
	
}
 
 
