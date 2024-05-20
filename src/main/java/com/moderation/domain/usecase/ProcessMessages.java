package com.moderation.domain.usecase;

import com.google.common.base.VerifyException;
import com.moderation.domain.entity.SingleMessageModerationResult;
import com.moderation.domain.entity.UserModerationResult;
import com.moderation.domain.repository.ResultFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProcessMessages {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMessages.class);

    private ParseMessageInput parseMessageInput;
    private ProcessSingleMessage processSingleMessage;
    private ResultFileRepository resultFileRepository;

    public ProcessMessages(ParseMessageInput parseMessageInput,
                           ProcessSingleMessage processSingleMessage,
                           ResultFileRepository resultFileRepository) {
        this.parseMessageInput = parseMessageInput;
        this.processSingleMessage = processSingleMessage;
        this.resultFileRepository = resultFileRepository;
    }

    public Mono<String> process(Flux<String> messagesFlux) {
        Flux<UserModerationResult> userResultsFlux = messagesFlux

                .map(parseMessageInput::parse)
                .onErrorContinue(VerifyException.class,
                        (ex, o) -> LOGGER.warn("Skipping line due to parsing error: {}", ex.getMessage()))

                .flatMap(processSingleMessage::process)
                .doOnNext(singleMessageResult -> LOGGER.debug("single message result: {}", singleMessageResult))

                .collect(Collectors.groupingBy(SingleMessageModerationResult::getUserId,
                        Collectors.collectingAndThen(Collectors.toList(), this::createUserModerationResult)))
                .flatMapIterable(map -> map.values())
                .doOnNext(userMessageResult -> LOGGER.debug("user message result: {}", userMessageResult));

        return resultFileRepository.saveResults(createResultsId(), userResultsFlux);
    }

    private String createResultsId() {
        return UUID.randomUUID().toString();
    }

    private UserModerationResult createUserModerationResult(List<SingleMessageModerationResult> resultList) {
        return new UserModerationResult(
                resultList.get(0).getUserId(),
                Long.valueOf(resultList.size()),
                resultList.stream().collect(Collectors.averagingDouble(SingleMessageModerationResult::getScore)));
    }

}
