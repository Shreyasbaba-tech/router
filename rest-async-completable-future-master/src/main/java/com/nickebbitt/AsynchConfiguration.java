package com.nickebbitt;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsynchConfiguration
{
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor()
    {
    	ExecutorService executorService = Executors.newCachedThreadPool();
		/*
		 * ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		 * executor.setCorePoolSize(3); executor.setMaxPoolSize(3);
		 * executor.setQueueCapacity(100);
		 * executor.setThreadNamePrefix("AsynchThread-"); executor.initialize();
		 */
        return executorService;
    }
}