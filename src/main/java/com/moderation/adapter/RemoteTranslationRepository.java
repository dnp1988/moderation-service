package com.moderation.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.moderation.adapter.entity.TranslationRequest;
import com.moderation.adapter.entity.TranslationResponse;
import com.moderation.domain.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Profile("!mocked")
@Repository
public class RemoteTranslationRepository extends AbstractCachedRepository<TranslationResponse, String> implements TranslationRepository {

    private WebClient webClient;

    public RemoteTranslationRepository(@Qualifier("translationClient") WebClient webClient,
                                       @Qualifier("translationCache") Cache<String, TranslationResponse> cache) {
        super(cache);
        this.webClient = webClient;
    }

    @Override
    public Mono<String> translateMessage(String messageText) {
        return getCachedValueOrMakeRequest(messageText);
    }

    @Override
    protected String mapResponseToValue(TranslationResponse response) {
        return response.getText();
    }

    @Override
    protected Mono<TranslationResponse> makeRequest(String messageText) {
        return webClient.post()
                .uri(AdapterConstants.TRANSLATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new TranslationRequest(messageText))
                .retrieve()
                .bodyToMono(TranslationResponse.class);
    }
}
