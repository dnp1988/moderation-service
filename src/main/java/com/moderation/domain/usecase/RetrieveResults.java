package com.moderation.domain.usecase;

import com.moderation.domain.repository.ResultFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class RetrieveResults {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveResults.class);

    private ResultFileRepository resultFileRepository;

    public RetrieveResults(ResultFileRepository resultFileRepository) {
        this.resultFileRepository = resultFileRepository;
    }

    public Flux<DataBuffer> retrieve(String resultsId) {
        return resultFileRepository.retrieveResults(resultsId);
    }
}
