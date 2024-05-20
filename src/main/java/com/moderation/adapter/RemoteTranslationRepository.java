package com.moderation.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.moderation.adapter.entity.TranslationRequest;
import com.moderation.adapter.entity.TranslationResponse;
import com.moderation.domain.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Repository
public class RemoteTranslationRepository implements TranslationRepository {

    private WebClient webClient;
    private Cache<String, TranslationResponse> cache;

    public RemoteTranslationRepository(@Qualifier("translationClient") WebClient webClient,
                                       @Qualifier("translationCache") Cache<String, TranslationResponse> cache) {
        this.webClient = webClient;
        this.cache = cache;
    }

    @Override
    public Mono<String> translateMessage(String messageText) {
        return Mono.justOrEmpty(cache.getIfPresent(messageText))
                .switchIfEmpty(Mono.defer(() -> requestTranslateMessage(messageText)
                        .doOnNext(response -> cache.put(messageText, response))))
                .map(TranslationResponse::getText);
    }

    public Mono<TranslationResponse> requestTranslateMessage(String messageText) {
        return webClient.post()
                .uri(AdapterConstants.TRANSLATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new TranslationRequest(messageText))
                .retrieve()
                .bodyToMono(TranslationResponse.class);
    }
}
