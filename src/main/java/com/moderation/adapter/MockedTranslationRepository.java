package com.moderation.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.moderation.adapter.entity.TranslationResponse;
import com.moderation.adapter.utils.RemoteMockUtils;
import com.moderation.domain.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Profile("mocked")
@Repository
public class MockedTranslationRepository extends AbstractCachedRepository<TranslationResponse, String> implements TranslationRepository {

    public MockedTranslationRepository(@Qualifier("translationCache") Cache<String, TranslationResponse> cache) {
        super(cache);
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
        String translatedMessage = RemoteMockUtils.getTranslatedMessage(messageText);
        return Mono.fromCallable(() -> new TranslationResponse(translatedMessage))
                .delayElement(Duration.ofMillis(RemoteMockUtils.getRandomResponseDelayInMillis()));
    }
}
