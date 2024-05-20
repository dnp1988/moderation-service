package com.moderation.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.moderation.adapter.entity.ScoringRequest;
import com.moderation.adapter.entity.ScoringResponse;
import com.moderation.domain.repository.ScoringRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Profile("!mocked")
@Repository
public class RemoteScoringRepository extends AbstractCachedRepository<ScoringResponse, Double> implements ScoringRepository {

    private WebClient webClient;

    public RemoteScoringRepository(@Qualifier("scoringClient") WebClient webClient,
                                   @Qualifier("scoringCache") Cache<String, ScoringResponse> cache) {
        super(cache);
        this.webClient = webClient;
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
        return webClient.post()
                .uri(AdapterConstants.SCORING_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ScoringRequest(messageText))
                .retrieve()
                .bodyToMono(ScoringResponse.class);
    }
}
