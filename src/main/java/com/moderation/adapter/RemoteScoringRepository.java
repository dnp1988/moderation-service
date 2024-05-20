package com.moderation.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.moderation.adapter.entity.ScoringRequest;
import com.moderation.adapter.entity.ScoringResponse;
import com.moderation.domain.repository.ScoringRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Repository
public class RemoteScoringRepository implements ScoringRepository {

    private WebClient webClient;
    private Cache<String, ScoringResponse> cache;

    public RemoteScoringRepository(@Qualifier("scoringClient") WebClient webClient,
                                   @Qualifier("scoringCache") Cache<String, ScoringResponse> cache) {
        this.webClient = webClient;
        this.cache = cache;
    }

    @Override
    public Mono<Double> scoreMessage(String messageText) {
        return Mono.justOrEmpty(cache.getIfPresent(messageText))
                .switchIfEmpty(Mono.defer(() -> requestScoringMessage(messageText)
                        .doOnNext(response -> cache.put(messageText, response))))
                .map(ScoringResponse::getScoringValue);
    }

    public Mono<ScoringResponse> requestScoringMessage(String messageText) {
        return webClient.post()
                .uri(AdapterConstants.SCORING_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ScoringRequest(messageText))
                .retrieve()
                .bodyToMono(ScoringResponse.class);
    }
}
