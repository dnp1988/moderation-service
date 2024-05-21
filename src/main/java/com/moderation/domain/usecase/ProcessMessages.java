package com.moderation.domain.usecase;

import com.google.common.base.VerifyException;
import com.moderation.domain.entity.SingleMessageModerationResult;
import com.moderation.domain.entity.UserModerationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcessMessages {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMessages.class);

    private ParseMessageInput parseMessageInput;
    private ProcessSingleMessage processSingleMessage;

    public ProcessMessages(ParseMessageInput parseMessageInput,
                           ProcessSingleMessage processSingleMessage) {
        this.parseMessageInput = parseMessageInput;
        this.processSingleMessage = processSingleMessage;
    }

    public Flux<UserModerationResult> process(Flux<String> messagesFlux) {
        return messagesFlux

                .map(parseMessageInput::parse)
                .onErrorContinue(VerifyException.class,
                        (ex, o) -> LOGGER.warn("Skipping line due to parsing error: {}", ex.getMessage()))

                .flatMap(processSingleMessage::process)
                .doOnNext(singleMessageResult -> LOGGER.debug("single message result: {}", singleMessageResult))

                .collect(Collectors.groupingBy(SingleMessageModerationResult::getUserId,
                        Collectors.collectingAndThen(Collectors.toList(), this::createUserModerationResult)))
                .flatMapIterable(map -> map.values())
                .doOnNext(userMessageResult -> LOGGER.debug("user message result: {}", userMessageResult));
    }

    private UserModerationResult createUserModerationResult(List<SingleMessageModerationResult> resultList) {
        return new UserModerationResult(
                resultList.get(0).getUserId(),
                Long.valueOf(resultList.size()),
                resultList.stream().collect(Collectors.averagingDouble(SingleMessageModerationResult::getScore)));
    }

}
