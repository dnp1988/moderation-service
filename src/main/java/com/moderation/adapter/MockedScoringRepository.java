package com.moderation.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.moderation.adapter.entity.ScoringResponse;
import com.moderation.adapter.utils.RemoteMockUtils;
import com.moderation.domain.repository.ScoringRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Profile("mocked")
@Repository
public class MockedScoringRepository extends AbstractCachedRepository<ScoringResponse, Double> implements ScoringRepository {

    public MockedScoringRepository(@Qualifier("scoringCache") Cache<String, ScoringResponse> cache) {
        super(cache);
    }

    @Override
    public Mono<Double> scoreMessage(String messageText) {
        return getCachedValueOrMakeRequest(messageText);
    }

    @Override
    protected Double mapResponseToValue(ScoringResponse response) {
        return response.getScoringValue();
    }

    @Override
    protected Mono<ScoringResponse> makeRequest(String messageText) {
        Double score = RemoteMockUtils.getScoringValue(messageText);
        return Mono.fromCallable(() -> new ScoringResponse(score))
                .delayElement(Duration.ofMillis(RemoteMockUtils.getRandomResponseDelayInMillis()));
    }
}
