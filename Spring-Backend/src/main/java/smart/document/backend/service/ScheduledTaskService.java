package smart.document.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Scheduled(fixedRate = 30000)
    public void scheduledTask() {
        log.info("Scheduled task executed at: {}", LocalDateTime.now());
    }
}
