package com.moderation.domain.repository;

import reactor.core.publisher.Mono;

public interface ScoringRepository {

    Mono<Double> scoreMessage(String messageText);
}
