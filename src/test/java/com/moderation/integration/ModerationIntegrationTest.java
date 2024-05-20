package com.moderation.integration;

import com.moderation.api.ApiConstants;
import com.moderation.api.ReviseMessagesResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureWebTestClient
@SpringBootTest(properties = {
        "spring.main.web-application-type=reactive",
        "moderation.clients.translation.baseUrl=http://localhost:8088",
        "moderation.clients.scoring.baseUrl=http://localhost:8088"
})
public class ModerationIntegrationTest {

    private static final int MOCK_SERVER_PORT = 8088;
    private static final String INPUT_CSV = "input.csv";
    private static final String OUTPUT_CSV = "output.csv";
    public static final String CSV_EXTENSION = ".csv";
    private static MockWebServer mockBackEnd;

    @TempDir
    private File tempDir;

    @Autowired
    private WebTestClient webClient;

    @BeforeAll
    public static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start(MOCK_SERVER_PORT);
        mockBackEnd.setDispatcher(new MockWebServerDispatcher());
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    public void testModerationsRevise() throws Exception {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part(ApiConstants.MESSAGES_ENDPOINT_FILE_MULTIPART, new ClassPathResource(INPUT_CSV), MediaType.TEXT_PLAIN);

        FluxExchangeResult<ReviseMessagesResponse> messagesResponseFlux = webClient.post()
                .uri(ApiConstants.MESSAGES_ENDPOINT_FULL_PATH)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .returnResult(ReviseMessagesResponse.class);

        ReviseMessagesResponse messagesResponse = messagesResponseFlux.getResponseBody().single().block(Duration.ZERO);

        FluxExchangeResult<DataBuffer> resultsResponseFlux = webClient.get()
                .uri(ApiConstants.RESULTS_ENDPOINT_FULL_PATH + "/" + messagesResponse.getResultsId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(DataBuffer.class);

        Path resultsFilePath = tempDir.toPath().resolve(messagesResponse.getResultsId() + CSV_EXTENSION);

        DataBufferUtils.write(resultsResponseFlux.getResponseBody(),
                        resultsFilePath,
                        StandardOpenOption.CREATE)
                .share().block();

        try(InputStream expected = new ClassPathResource(OUTPUT_CSV).getInputStream();
            InputStream result = new FileInputStream(resultsFilePath.toFile())) {
            assertTrue(IOUtils.contentEquals(expected, result));
        }
    }

}
