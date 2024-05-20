package com.moderation.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import com.moderation.adapter.entity.ScoringResponse;
import com.moderation.adapter.entity.TranslationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class ClientsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientsConfiguration.class);

    @Bean
    @Qualifier("translationClient")
    public WebClient translationClient(@Value("${moderation.clients.translation.baseUrl}") String baseUrl) {
        return WebClient.create(baseUrl);
    }

    @Bean
    @Qualifier("scoringClient")
    public WebClient scoringClient(@Value("${moderation.clients.scoring.baseUrl}") String baseUrl) {
        return WebClient.create(baseUrl);
    }

    @Bean
    @Qualifier("scoringCache")
    public Cache<String, ScoringResponse> scoringCache(@Value("${moderation.clients.scoring.cache.ttl:600}") Long ttlSeconds,
                                                       @Value("${moderation.clients.scoring.cache.size:10000}") Long size) {
        return Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofSeconds(ttlSeconds))
                .maximumSize(size)
                .evictionListener((key, item, cause) -> LOGGER.debug("scoringCache key {} was evicted", key))
                .removalListener((key, item, cause) -> LOGGER.debug("scoringCache key {} was removed", key))
                .scheduler(Scheduler.systemScheduler())
                .build();
    }

    @Bean
    @Qualifier("translationCache")
    public Cache<String, TranslationResponse> translationCache(@Value("${moderation.clients.translation.cache.ttl:600}") Long ttlSeconds,
                                                       @Value("${moderation.clients.translation.cache.size:10000}") Long size) {
        return Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofSeconds(ttlSeconds))
                .maximumSize(size)
                .evictionListener((key, item, cause) -> LOGGER.debug("scoringCache key {} was evicted", key))
                .removalListener((key, item, cause) -> LOGGER.debug("scoringCache key {} was removed", key))
                .scheduler(Scheduler.systemScheduler())
                .build();
    }
}
