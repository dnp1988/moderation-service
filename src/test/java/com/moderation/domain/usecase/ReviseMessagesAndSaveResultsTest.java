package com.moderation.domain.usecase;

import com.moderation.domain.entity.UserModerationResult;
import com.moderation.domain.repository.ResultFileRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReviseMessagesAndSaveResultsTest {

    private static final String RESULTS_ID = "123";
    private static final String MESSAGE_INPUT_LINE = "1,Message1";

    private ProcessMessages processMessages = mock(ProcessMessages.class);
    private CreateResultsId createResultsId = mock(CreateResultsId.class);
    private ResultFileRepository resultFileRepository = mock(ResultFileRepository.class);
    private ReviseMessagesAndSaveResults useCase = new ReviseMessagesAndSaveResults(
            processMessages,
            createResultsId,
            resultFileRepository);

    @Test
    public void testReviseMessagesAndSaveResults() {
        Flux<String> messagesFlux = Flux.just(MESSAGE_INPUT_LINE);
        Flux<UserModerationResult> userModerationResultFluxMock = Flux.just(mock(UserModerationResult.class));

        PublisherProbe<String> stringProbe = PublisherProbe.of(Mono.just(RESULTS_ID));
        when(resultFileRepository.saveResults(RESULTS_ID, userModerationResultFluxMock))
                .thenReturn(stringProbe.mono());

        when(createResultsId.create())
                .thenReturn(RESULTS_ID);

        when(processMessages.process(messagesFlux))
                .thenReturn(userModerationResultFluxMock);

        StepVerifier.create(useCase.revise(messagesFlux))
                .expectNext(RESULTS_ID)
                .verifyComplete();

        verify(resultFileRepository).saveResults(RESULTS_ID, userModerationResultFluxMock);
        verify(createResultsId).create();
        verify(processMessages).process(messagesFlux);
        stringProbe.assertWasSubscribed();
    }
}
