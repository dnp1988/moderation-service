package com.moderation.domain.usecase;

import com.moderation.domain.entity.MessageInput;
import com.moderation.domain.entity.SingleMessageModerationResult;
import com.moderation.domain.repository.ScoringRepository;
import com.moderation.domain.repository.TranslationRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ProcessSingleMessage {

    private TranslationRepository translationRepository;

    private ScoringRepository scoringRepository;

    public ProcessSingleMessage(TranslationRepository translationRepository,
                                ScoringRepository scoringRepository) {
        this.translationRepository = translationRepository;
        this.scoringRepository = scoringRepository;
    }

    public Mono<SingleMessageModerationResult> process(MessageInput messageInput) {
        SingleMessageModerationResult moderationResult = new SingleMessageModerationResult();
        moderationResult.setUserId(messageInput.getUserId());
        moderationResult.setMessageText(messageInput.getMessageText());

        return Mono.just(moderationResult)
                .flatMap(result -> translationRepository.translateMessage(result.getMessageText())
                        .map(translatedMessage -> result.setMessageText(translatedMessage)))
                .flatMap(result -> scoringRepository.scoreMessage(result.getMessageText())
                        .map(scoreValue -> result.setScore(scoreValue)));
    }
}
