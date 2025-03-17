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

    public Mono<String> search(String index, String query) {
        return webClient.get()
                .uri("/{index}/_search?q={query}", index, query)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> indexDocument(String index, String id, String document) {
        return webClient.get()
                .uri("/{index}/_doc/{id}", index, id)
                .retrieve()
                .bodyToMono(String.class);
    }
}
