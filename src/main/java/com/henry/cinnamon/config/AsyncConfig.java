package com.henry.cinnamon.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {


    @Bean(name = "ingestionExecutor")


    public Executor ingestionExecutor( ){

        int cores = Runtime.getRuntime().availableProcessors();

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(cores);
        threadPoolTaskExecutor.setMaxPoolSize(cores*2);
        threadPoolTaskExecutor.setQueueCapacity(500);
        threadPoolTaskExecutor.setThreadNamePrefix("ingest-worker-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;

    }
}
