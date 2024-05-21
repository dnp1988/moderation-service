package com.moderation.domain.usecase;

import com.moderation.domain.repository.ResultFileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RetrieveResultsTest {

    private static final String RESULTS_ID = "123";

    private ResultFileRepository resultFileRepository = mock(ResultFileRepository.class);
    private RetrieveResults useCase = new RetrieveResults(resultFileRepository);

    @Test
    public void testRetrieveResults() {
        Flux<DataBuffer> dataBufferFluxMock = Flux.just(mock(DataBuffer.class));
        when(resultFileRepository.retrieveResults(RESULTS_ID)).thenReturn(dataBufferFluxMock);
        assertEquals(dataBufferFluxMock, useCase.retrieve(RESULTS_ID));
        verify(resultFileRepository).retrieveResults(RESULTS_ID);
    }
}
