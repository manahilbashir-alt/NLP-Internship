package smart.document.backend.service;

import smart.document.backend.entity.Document;
import smart.document.backend.repository.DocumentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final NotificationClientService notificationClientService;

    public DocumentService(
            DocumentRepository documentRepository,
            NotificationClientService notificationClientService) {

        this.documentRepository = documentRepository;
        this.notificationClientService = notificationClientService;
    }

    public Document createDocument(
            String title,
            String content,
            String ownerEmail) {

        Document document = new Document(
                title,
                content,
                ownerEmail
        );

        Document saved = documentRepository.save(document);

        notificationClientService
                .notify("New document created: " + saved.getTitle())
                .subscribe();

        return saved;
    }

    public List<Document> getUserDocuments(String ownerEmail) {
        return documentRepository.findByOwnerEmail(ownerEmail);
    }

    @Cacheable(value = "documents", key = "#id + '-' + #ownerEmail")
    public Document getDocument(Long id, String ownerEmail) {

        log.debug("Fetching document {} for {} from database", id, ownerEmail);

        return documentRepository
                .findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(
                        () -> new RuntimeException("Document not found")
                );
    }

    @CacheEvict(value = "documents", key = "#id + '-' + #ownerEmail")
    public Document updateDocument(
            Long id,
            String title,
            String content,
            String ownerEmail) {

        Document document = documentRepository
                .findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(
                        () -> new RuntimeException("Document not found")
                );

        document.setTitle(title);
        document.setContent(content);

        return documentRepository.save(document);
    }

    @CacheEvict(value = "documents", key = "#id + '-' + #ownerEmail")
    public void deleteDocument(Long id, String ownerEmail) {

        Document document =
                documentRepository
                        .findByIdAndOwnerEmail(id, ownerEmail)
                        .orElseThrow(
                                () -> new RuntimeException("Document not found")
                        );

        documentRepository.delete(document);
    }
}