package smart.document.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledDocumentService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledDocumentService.class);

    @Scheduled(fixedRate = 60000)
    public void processDocuments() {
        log.info("Scheduled document processing is running...");
    }
}
