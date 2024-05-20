package com.moderation.controller;

import com.moderation.api.ApiConstants;
import com.moderation.api.ReviseMessagesResponse;
import com.moderation.domain.usecase.ProcessMessages;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.PublisherProbe;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ModerationController.class)
public class ModerationControllerTest {

    private static final String INPUT_CSV = "short-input.csv";
    private static final String RESULTS_ID = "123";

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private ProcessMessages processMessages;

    @Test
    public void testPostModerationsReviseOk() {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part(ApiConstants.MESSAGES_ENDPOINT_FILE_MULTIPART, new ClassPathResource(INPUT_CSV));

        PublisherProbe<String> stringProbe = PublisherProbe.of(Mono.just(RESULTS_ID));
        when(processMessages.process(any(Flux.class))).thenReturn(stringProbe.mono());

        webClient.post()
                .uri(ApiConstants.MESSAGES_ENDPOINT_FULL_PATH)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ReviseMessagesResponse.class)
                .isEqualTo(new ReviseMessagesResponse(RESULTS_ID));

        verify(processMessages).process(any(Flux.class));
        stringProbe.assertWasSubscribed();
    }
}
