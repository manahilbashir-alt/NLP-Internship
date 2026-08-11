package smart.document.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class NotificationClientService {

    private final WebClient webClient;

    public NotificationClientService(
            WebClient.Builder builder,
            @Value("${notification.service.url:http://localhost:8081}") String notificationServiceUrl) {

        this.webClient = builder
                .baseUrl(notificationServiceUrl)
                .build();
    }

    public Mono<String> notify(String message) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/notifications")
                        .queryParam("message", message)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("Notification Service unavailable");
    }
}
