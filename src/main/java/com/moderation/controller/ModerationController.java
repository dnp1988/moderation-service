package com.moderation.controller;

import com.moderation.api.ApiConstants;
import com.moderation.api.ReviseMessagesResponse;
import com.moderation.domain.repository.ResultFileRepository;
import com.moderation.domain.usecase.ProcessMessages;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.StringDecoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Collections;

@RestController
@RequestMapping(ApiConstants.MODERATION_CONTROLLER_PATH)
public class ModerationController {

    private ProcessMessages processMessages;
    private ResultFileRepository resultFileRepository;
    private StringDecoder stringDecoder;

    public ModerationController(ProcessMessages processMessages, ResultFileRepository resultFileRepository) {
        this.processMessages = processMessages;
        this.resultFileRepository = resultFileRepository;
        this.stringDecoder = StringDecoder.textPlainOnly();
    }

    @PostMapping(value = ApiConstants.MESSAGES_ENDPOINT_PATH,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<ReviseMessagesResponse>> reviseMessagesFile(
            @RequestPart(ApiConstants.MESSAGES_ENDPOINT_FILE_MULTIPART) Mono<FilePart> filePartMono) {
        Flux<String> fileLinesFlux = filePartMono.flatMapMany(filePart -> stringDecoder.decode(filePart.content(),
                ResolvableType.NONE,
                MimeTypeUtils.TEXT_PLAIN,
                Collections.emptyMap()
        ));
        return ResponseEntity.ok()
                .body(processMessages.process(fileLinesFlux).map(ReviseMessagesResponse::new));
    }

    @GetMapping(value = ApiConstants.RESULTS_ENDPOINT_PATH + "/{id}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Flux<DataBuffer>> retrieveResultsFile(@PathVariable("id") String resultsId) {
        Flux<DataBuffer> file = resultFileRepository.retrieveResults(resultsId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", resultsId))
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
    }
}
