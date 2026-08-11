package smart.document.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(
            topics = "document-topic",
            groupId = "smart-document-group"
    )
    public void consumeDocumentEvent(String message) {
        log.info("Kafka event received: {}", message);
    }
}
