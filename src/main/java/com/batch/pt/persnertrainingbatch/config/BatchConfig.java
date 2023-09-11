package com.batch.pt.persnertrainingbatch.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableBatchProcessing // 배치 기능 활성화
@Configuration
public class BatchConfig {
    
}
