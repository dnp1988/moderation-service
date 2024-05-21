package com.moderation.domain.usecase;

import com.moderation.domain.entity.MessageInput;
import com.moderation.domain.entity.SingleMessageModerationResult;
import com.moderation.domain.entity.UserModerationResult;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProcessMessagesTest {

    private static final String USER_ID_1 = "1";
    private static final String USER_ID_2 = "2";

    private static final String MESSAGE_1 = "Message1";
    private static final String MESSAGE_2 = "Message2";
    private static final String MESSAGE_3 = "Message3";
    private static final String MESSAGE_4 = "Message4";

    private static final String TRANSLATED_PREFIX = "Translated ";

    private ParseMessageInput parseMessageInput = mock(ParseMessageInput.class);
    private ProcessSingleMessage processSingleMessage = mock(ProcessSingleMessage.class);
    private ProcessMessages useCase = new ProcessMessages(
            parseMessageInput,
            processSingleMessage);

    @Test
    public void testProcessMessages() {
        PublisherProbe<SingleMessageModerationResult> messageProbe1 = mockMessageProcess(USER_ID_1, MESSAGE_1, 0.123);
        PublisherProbe<SingleMessageModerationResult> messageProbe2 = mockMessageProcess(USER_ID_1, MESSAGE_2, 0.245);
        PublisherProbe<SingleMessageModerationResult> messageProbe3 = mockMessageProcess(USER_ID_2, MESSAGE_3, 0.678);
        PublisherProbe<SingleMessageModerationResult> messageProbe4 = mockMessageProcess(USER_ID_2, MESSAGE_4, 0.9);

        Flux<String> messageInputLinesFlux = Flux.fromIterable(List.of(
                createMessageInputLine(USER_ID_1, MESSAGE_1),
                createMessageInputLine(USER_ID_1, MESSAGE_2),
                createMessageInputLine(USER_ID_2, MESSAGE_3),
                createMessageInputLine(USER_ID_2, MESSAGE_4)
        ));

        StepVerifier.create(useCase.process(messageInputLinesFlux))
                .expectNext(new UserModerationResult(USER_ID_1, 2L, 0.184))
                .expectNext(new UserModerationResult(USER_ID_2, 2L, 0.789))
                .verifyComplete();

        verifyMessageProcess(USER_ID_1, MESSAGE_1, messageProbe1);
        verifyMessageProcess(USER_ID_1, MESSAGE_2, messageProbe2);
        verifyMessageProcess(USER_ID_2, MESSAGE_3, messageProbe3);
        verifyMessageProcess(USER_ID_2, MESSAGE_4, messageProbe4);
    }

    private PublisherProbe<SingleMessageModerationResult> mockMessageProcess(String userId,
                                                                             String messageText,
                                                                             Double scoring) {
        MessageInput messageInput = new MessageInput(userId, messageText);

        when(parseMessageInput.parse(createMessageInputLine(userId, messageText)))
                .thenReturn(messageInput);

        String translatedMessage = TRANSLATED_PREFIX + messageText;

        SingleMessageModerationResult messageModerationResult = new SingleMessageModerationResult(
                userId,
                translatedMessage,
                scoring);

        PublisherProbe<SingleMessageModerationResult> singleMessageModerationResultProbe = PublisherProbe
                .of(Mono.just(messageModerationResult));
        when(processSingleMessage.process(messageInput))
                .thenReturn(singleMessageModerationResultProbe.mono());

        return singleMessageModerationResultProbe;
    }

    private void verifyMessageProcess(String userId,
                                      String messageText,
                                      PublisherProbe<SingleMessageModerationResult> messageProbe) {
        verify(parseMessageInput).parse(createMessageInputLine(userId, messageText));
        verify(processSingleMessage).process(new MessageInput(userId, messageText));
        messageProbe.assertWasSubscribed();
    }

    private String createMessageInputLine(String userId, String messageText) {
        return String.format("%s,%s", userId, messageText);
    }
}
