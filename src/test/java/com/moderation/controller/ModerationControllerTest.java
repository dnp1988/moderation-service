package com.moderation.controller;

import com.moderation.api.ApiConstants;
import com.moderation.api.ReviseMessagesResponse;
import com.moderation.domain.usecase.RetrieveResults;
import com.moderation.domain.usecase.ReviseMessagesAndSaveResults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.PublisherProbe;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ModerationController.class)
public class ModerationControllerTest {

    private static final String INPUT_CSV = "short-input.csv";
    private static final String RESULTS_ID = "123";
    private static final String RESULTS_FILE_CONTENT = "40,2478,0.4780\n" + "20,2540,0.4844\n" + "21,2426,0.4835";
    private static final int BUFFER_SIZE = 128;

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private ReviseMessagesAndSaveResults reviseMessagesAndSaveResults;

    @MockBean
    private RetrieveResults retrieveResults;

    @Test
    public void testReviseMessagesFile() {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part(ApiConstants.MESSAGES_ENDPOINT_FILE_MULTIPART, new ClassPathResource(INPUT_CSV));

        PublisherProbe<String> stringProbe = PublisherProbe.of(Mono.just(RESULTS_ID));
        when(reviseMessagesAndSaveResults.revise(any(Flux.class))).thenReturn(stringProbe.mono());

        webClient.post()
                .uri(ApiConstants.MESSAGES_ENDPOINT_FULL_PATH)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReviseMessagesResponse.class).isEqualTo(new ReviseMessagesResponse(RESULTS_ID));

        verify(reviseMessagesAndSaveResults).revise(any(Flux.class));
        stringProbe.assertWasSubscribed();
    }

    @Test
    public void testRetrieveResultsFile() throws IOException {
        DataBuffer buffer = getResultFileDataBuffer();

        PublisherProbe<DataBuffer> dataBufferProbe = PublisherProbe.of(Flux.just(buffer));
        when(retrieveResults.retrieve(RESULTS_ID)).thenReturn(dataBufferProbe.flux());

        FluxExchangeResult<DataBuffer> resultsResponseFlux = webClient.get()
                .uri(ApiConstants.RESULTS_ENDPOINT_FULL_PATH + "/" + RESULTS_ID)
                .exchange()
                .expectStatus().isOk()
                .returnResult(DataBuffer.class);

        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream(BUFFER_SIZE)) {
            DataBufferUtils.write(resultsResponseFlux.getResponseBody(), outputStream).share().collectList().block();
            String responseBodyAsString = outputStream.toString(StandardCharsets.UTF_8);
            Assertions.assertEquals(RESULTS_FILE_CONTENT, responseBodyAsString);
        }

        verify(retrieveResults).retrieve(RESULTS_ID);
        dataBufferProbe.assertWasSubscribed();
    }

    private DataBuffer getResultFileDataBuffer() {
        DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
        DataBuffer buffer = bufferFactory.allocateBuffer(BUFFER_SIZE);
        buffer.write(RESULTS_FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
        return buffer;
    }
}
