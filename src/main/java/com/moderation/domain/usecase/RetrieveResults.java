package com.moderation.domain.usecase;

import com.moderation.domain.repository.ResultFileRepository;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class RetrieveResults {

    private ResultFileRepository resultFileRepository;

    public RetrieveResults(ResultFileRepository resultFileRepository) {
        this.resultFileRepository = resultFileRepository;
    }

    public Flux<DataBuffer> retrieve(String resultsId) {
        return resultFileRepository.retrieveResults(resultsId);
    }
}
