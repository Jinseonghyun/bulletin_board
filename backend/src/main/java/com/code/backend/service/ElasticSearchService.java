package com.code.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ElasticSearchService {

    private final WebClient webClient;

    @Autowired
    public ElasticSearchService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> articleSearch(String index, String query) {
        return webClient.get()
                .uri("/article/_search?q={query}", index, query)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> indexArticleDocument(String id, String document) {
        return webClient.put()
                .uri("/article/_doc/{id}", id)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .bodyValue(document)
                .retrieve()
                .bodyToMono(String.class);
    }
}
