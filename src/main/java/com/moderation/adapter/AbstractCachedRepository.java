package com.moderation.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import reactor.core.publisher.Mono;

public abstract class AbstractCachedRepository<R,V> {

    protected Cache<String, R> cache;

    protected AbstractCachedRepository(Cache<String, R> cache) {
        this.cache = cache;
    }

    protected Mono<V> getCachedValueOrMakeRequest(String messageText) {
        return Mono.justOrEmpty(cache.getIfPresent(messageText))
                .switchIfEmpty(Mono.defer(() -> makeRequest(messageText)
                        .doOnNext(response -> cache.put(messageText, response))))
                .map(this::mapResponseToValue);
    }

    protected abstract V mapResponseToValue(R response);

    protected abstract Mono<R> makeRequest(String messageText);
}
