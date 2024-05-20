package com.moderation.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moderation.adapter.AdapterConstants;
import com.moderation.adapter.entity.ScoringRequest;
import com.moderation.adapter.entity.ScoringResponse;
import com.moderation.adapter.entity.TranslationRequest;
import com.moderation.adapter.entity.TranslationResponse;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class MockWebServerDispatcher extends Dispatcher {

    public static final String TRANSLATED_PREFIX = "translated ";
    private ObjectMapper mapper = new ObjectMapper();

    @NotNull
    @Override
    public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
        switch (recordedRequest.getPath()) {
            case AdapterConstants.TRANSLATION_PATH:
                return createTranslationMockResponse(recordedRequest);
            case AdapterConstants.SCORING_PATH:
                return createScoringMockResponse(recordedRequest);
        }
        return createNotFoundMockResponse();
    }

    @NotNull
    private static MockResponse createNotFoundMockResponse() {
        return new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value());
    }

    @NotNull
    private MockResponse createScoringMockResponse(RecordedRequest recordedRequest) {
        ScoringRequest request = fromJsonString(recordedRequest.getBody().readUtf8(), ScoringRequest.class);

        Double score = 1.0 * Math.abs(request.getText().hashCode()) / Integer.MAX_VALUE;

        return new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(toJsonString(new ScoringResponse(score)));
    }

    @NotNull
    private MockResponse createTranslationMockResponse(RecordedRequest recordedRequest) {
        TranslationRequest request = fromJsonString(recordedRequest.getBody().readUtf8(), TranslationRequest.class);
        return new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(toJsonString(new TranslationResponse(TRANSLATED_PREFIX + request.getText())));
    }

    private String toJsonString(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private <T> T fromJsonString(String value, Class<T> valueType) {
        try {
            return mapper.readValue(value, valueType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
