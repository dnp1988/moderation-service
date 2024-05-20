package com.moderation.domain.repository;

import reactor.core.publisher.Mono;

public interface TranslationRepository {

    Mono<String> translateMessage(String messageText);
}
