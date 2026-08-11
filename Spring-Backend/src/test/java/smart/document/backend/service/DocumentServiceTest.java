package smart.document.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;

import smart.document.backend.entity.Document;
import smart.document.backend.repository.DocumentRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private NotificationClientService notificationClientService;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(documentRepository, notificationClientService);
        lenient().when(notificationClientService.notify(anyString()))
                .thenReturn(Mono.just("ok"));
    }

    @Test
    void createDocumentSavesAndTriggersNotification() {
        Document toSave = new Document("Title", "Content", "owner@example.com");
        Document saved = new Document("Title", "Content", "owner@example.com");
        saved.setId(1L);

        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        Document result = documentService.createDocument("Title", "Content", "owner@example.com");

        assertEquals(1L, result.getId());
        verify(documentRepository).save(any(Document.class));
        verify(notificationClientService).notify(contains("Title"));
    }

    @Test
    void getDocumentThrowsWhenNotFound() {
        when(documentRepository.findByIdAndOwnerEmail(99L, "owner@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> documentService.getDocument(99L, "owner@example.com"));
    }

    @Test
    void getUserDocumentsReturnsOnlyOwnersDocuments() {
        Document doc = new Document("Title", "Content", "owner@example.com");
        when(documentRepository.findByOwnerEmail("owner@example.com"))
                .thenReturn(List.of(doc));

        List<Document> result = documentService.getUserDocuments("owner@example.com");

        assertEquals(1, result.size());
        verify(documentRepository).findByOwnerEmail("owner@example.com");
    }
}
