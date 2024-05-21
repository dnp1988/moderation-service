package com.moderation.domain.usecase;

import com.moderation.domain.entity.MessageInput;
import com.moderation.domain.entity.SingleMessageModerationResult;
import com.moderation.domain.repository.ScoringRepository;
import com.moderation.domain.repository.TranslationRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProcessSingleMessageTest {

    private static final String USER_ID = "1";
    private static final String MESSAGE_TEXT = "Message";
    private static final String TRANSLATED_MESSAGE_TEXT = "TranslatedMessage";
    private static final Double SCORING_VALUE = 0.5;

    private TranslationRepository translationRepository = mock(TranslationRepository.class);
    private ScoringRepository scoringRepository = mock(ScoringRepository.class);
    private ProcessSingleMessage useCase = new ProcessSingleMessage(translationRepository, scoringRepository);

    @Test
    public void testProcessSingleMessage() {
        MessageInput messageInput = new MessageInput(USER_ID, MESSAGE_TEXT);
        SingleMessageModerationResult expectedResult = new SingleMessageModerationResult(USER_ID, TRANSLATED_MESSAGE_TEXT, SCORING_VALUE);

        PublisherProbe<String> stringProbe = PublisherProbe.of(Mono.just(TRANSLATED_MESSAGE_TEXT));
        when(translationRepository.translateMessage(MESSAGE_TEXT)).thenReturn(stringProbe.mono());

        PublisherProbe<Double> doubleProbe = PublisherProbe.of(Mono.just(SCORING_VALUE));
        when(scoringRepository.scoreMessage(TRANSLATED_MESSAGE_TEXT)).thenReturn(doubleProbe.mono());

        StepVerifier.create(useCase.process(messageInput))
                .expectNext(expectedResult)
                .verifyComplete();

        verify(translationRepository).translateMessage(MESSAGE_TEXT);
        verify(scoringRepository).scoreMessage(TRANSLATED_MESSAGE_TEXT);
        stringProbe.assertWasSubscribed();
        doubleProbe.assertWasSubscribed();
    }
}
