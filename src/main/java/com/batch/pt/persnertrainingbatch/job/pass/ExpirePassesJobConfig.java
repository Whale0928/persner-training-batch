package com.batch.pt.persnertrainingbatch.job.pass;

import com.batch.pt.persnertrainingbatch.repository.pass.PassEntity;
import com.batch.pt.persnertrainingbatch.repository.pass.PassStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 이용권 만료 Job 설정
 */
@Configuration
@RequiredArgsConstructor
public class ExpirePassesJobConfig {
    private final int CHUNK_SIZE = 50;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;


    /**
     * Job 설정 <br>
     * 1. jobBuilderFactory.get("expirePassesJob") : Job 이름 설정 <br>
     * 2. start(expirePassesStep()) : Step 설정 <br>
     */
    @Bean
    public Job expirePassesJob() {
        return this.jobBuilderFactory.get("expirePassesJob")
                .start(expirePassesStep())
                .build();
    }

    /**
     * Step 설정 <br>
     * 1. stepBuilderFactory.get("expirePassesStep") : Step 이름 설정 <br>
     * 2. <PassEntity, PassEntity>chunk(CHUNK_SIZE) : input type, output type , chunk size 설정 <br>
     * 3. reader(expirePassesReader()) : reader 설정 <br>
     * 4. writer(expirePassesWriter()) : writer 설정 <br>
     */
    @Bean
    public Step expirePassesStep() {
        return this.stepBuilderFactory.get("expirePassesStep")
                .<PassEntity, PassEntity>chunk(CHUNK_SIZE)
                .reader(expirePassesReader())
                .processor(entityItemProcessor())
                .writer(expirePassesWriter())
                .build();
    }


    /**
     * 작업할 데이터를 읽어오는 Reader 설정 <br>
     * Spring 4.3에서부터 추가된 JpaCursorItemReader<br>
     * 페이징 기법보다 높은 성능으로 데이터 변경에 무관한 무결성 조회가 가능<br>
     */
    @Bean
    @StepScope
    public JpaCursorItemReader<PassEntity> expirePassesReader() {
        return new JpaCursorItemReaderBuilder<PassEntity>()
                .name("expirePassesReader")
                .entityManagerFactory(entityManagerFactory) // 엔티티 매니저를 설정해줘야 한다.
                .queryString("select p " +
                        "from PassEntity p " +
                        "where p.status = :status " +
                        "and p.endedAt <= :endedAt")
                .parameterValues(Map.of("status", PassStatus.PROGRESS, "endedAt", LocalDateTime.now()))
                .build();
    }

    /**
     * 데이터를 가공하는 Processor 설정 <br>
     * 1. ItemProcessor<PassEntity, PassEntity> : input type, output type <br>
     * 하나의 작업내부에서 할 동작을 정의 <br>
     * 2. passEntity.setStatus(PassStatus.EXPIRED) : 만료 상태로 변경 <br>
     * 3. passEntity.setExpiredAt(LocalDateTime.now()) : 만료 시간 설정 <br>
     */
    @Bean
    public ItemProcessor<PassEntity, PassEntity> entityItemProcessor() {
        return passEntity -> {
            passEntity.setStatus(PassStatus.EXPIRED);
            passEntity.setExpiredAt(LocalDateTime.now());
            return passEntity;
        };
    }

    /**
     * 작업한 데이터를 저장하는 Writer 설정 <br>
     */
    @Bean
    public JpaItemWriter<PassEntity> expirePassesWriter() {
        return new JpaItemWriterBuilder<PassEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
