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

        // Cap to 1 thread to prevent concurrent ingestion OOM on memory-constrained deployments (e.g. Render 512MB)

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(1);
        threadPoolTaskExecutor.setQueueCapacity(10);
        threadPoolTaskExecutor.setThreadNamePrefix("ingest-worker-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;

    }
}
