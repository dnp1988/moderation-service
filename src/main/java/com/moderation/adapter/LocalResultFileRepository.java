package com.moderation.adapter;

import com.moderation.adapter.exception.RepositoryConfigurationException;
import com.moderation.domain.entity.UserModerationResult;
import com.moderation.domain.repository.ResultFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.NoSuchElementException;

@Repository
public class LocalResultFileRepository implements ResultFileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalResultFileRepository.class);
    private static final String DIRECTORY_PREFIX = "moderation";
    private static final String VALUE_SEPARATOR = ",";
    private static final String CSV_EXTENSION = ".csv";
    private static final int BUFFER_SIZE = 4096;

    private Path baseLocalDirectory;
    private DefaultDataBufferFactory bufferFactory;
    private CharSequenceEncoder encoder;

    public LocalResultFileRepository() {
        this.baseLocalDirectory = createBaseLocalDirectory();
        this.bufferFactory = new DefaultDataBufferFactory();
        this.encoder = CharSequenceEncoder.textPlainOnly();
    }

    private static Path createBaseLocalDirectory() throws RepositoryConfigurationException {
        try {
            Path basePath = Files.createTempDirectory(DIRECTORY_PREFIX);
            LOGGER.info("Created Result Base Local Directory ({})", basePath.toAbsolutePath());
            return basePath;
        } catch (IOException e) {
            String message = String.format("Could not set base local directory");
            LOGGER.error(message, e);
            throw new RepositoryConfigurationException(message, e);
        }
    }

    @Override
    public Mono<String> saveResults(String resultsId, Flux<UserModerationResult> userModerationResults) {
        Flux<DataBuffer> dataBufferFlux = userModerationResults
                .map(result -> encoder.encodeValue(createUserResultLine(result),
                        bufferFactory,
                        ResolvableType.NONE,
                        MimeTypeUtils.TEXT_PLAIN,
                        Collections.emptyMap()));

        Path resultPath = baseLocalDirectory.resolve(resultsId + CSV_EXTENSION);
        LOGGER.info("Saving Result File with path ({})", resultPath.toAbsolutePath());

        return DataBufferUtils.write(dataBufferFlux, resultPath, StandardOpenOption.CREATE_NEW)
                .then(Mono.just(resultsId));
    }

    private String createUserResultLine(UserModerationResult result) {
        return new StringBuilder()
                .append(result.getUserId())
                .append(VALUE_SEPARATOR)
                .append(result.getTotalMessages())
                .append(VALUE_SEPARATOR)
                .append(result.getAvgScore())
                .append(System.lineSeparator())
                .toString();
    }

    @Override
    public Flux<DataBuffer> retrieveResults(String resultsId) {

        Path resultPath = baseLocalDirectory.resolve(resultsId + CSV_EXTENSION);
        LOGGER.info("Retrieving Result File with path ({})", resultPath.toAbsolutePath());

        if(!resultPath.toFile().exists()) {
            LOGGER.warn("Result File with path ({}) NOT FOUND", resultPath.toAbsolutePath());
            return Flux.error(new NoSuchElementException("Result File not found"));
        }

        return DataBufferUtils.read(resultPath, new DefaultDataBufferFactory(), BUFFER_SIZE);
    }
}
