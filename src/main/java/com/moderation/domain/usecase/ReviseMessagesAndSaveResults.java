package com.moderation.domain.usecase;

import com.moderation.domain.repository.ResultFileRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReviseMessagesAndSaveResults {

    private ProcessMessages processMessages;
    private ResultFileRepository resultFileRepository;
    private CreateResultsId createResultsId;

    public ReviseMessagesAndSaveResults(ProcessMessages processMessages,
                                        CreateResultsId createResultsId,
                                        ResultFileRepository resultFileRepository) {
        this.processMessages = processMessages;
        this.createResultsId = createResultsId;
        this.resultFileRepository = resultFileRepository;
    }

    public Mono<String> revise(Flux<String> messagesFlux) {
        return resultFileRepository.saveResults(createResultsId.create(), processMessages.process(messagesFlux));
    }

}
