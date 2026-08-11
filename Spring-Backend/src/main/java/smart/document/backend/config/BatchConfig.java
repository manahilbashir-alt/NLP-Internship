package smart.document.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    @Bean
    public Job documentBatchJob(
            JobRepository jobRepository,
            Step documentBatchStep) {

        return new JobBuilder("documentBatchJob", jobRepository)
                .start(documentBatchStep)
                .build();
    }

    @Bean
    public Step documentBatchStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {

        return new StepBuilder("documentBatchStep", jobRepository)
                .tasklet(
                        (contribution, chunkContext) -> {
                            log.info("Batch job executed successfully");

                            return RepeatStatus.FINISHED;
                        },
                        transactionManager
                )
                .build();
    }
}