package com.moderation.domain.repository;

import com.moderation.domain.entity.UserModerationResult;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ResultFileRepository {

    Mono<String> saveResults(String resultsId, Flux<UserModerationResult> userModerationResults);

    Flux<DataBuffer> retrieveResults(String resultsId);
}
