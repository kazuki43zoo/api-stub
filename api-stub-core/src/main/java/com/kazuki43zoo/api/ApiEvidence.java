package com.kazuki43zoo.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

class ApiEvidence {
    private static final DateTimeFormatter DIR_NAME_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("uuuuMMddHHmmssSSS");
    private static ObjectMapper objectMapperForLog = Jackson2ObjectMapperBuilder.json().indentOutput(false).build();
    private static ObjectMapper objectMapperForFile = Jackson2ObjectMapperBuilder.json().indentOutput(true).build();

    private final Path dir;
    @Getter
    private final Logger logger;
    private final String contentExtension;
    private final ApiStubProperties properties;

    ApiEvidence(ApiStubProperties properties, String method, String path, String correlationId, String contentExtension) {
        this.dir = Paths.get(properties.getEvidence().getDir(),
                path, method, LocalDateTime.now().format(DIR_NAME_DATE_TIME_FORMAT) + "_" + correlationId);
        this.logger = LoggerFactory.getLogger(method + " " + path);
        if (!properties.getEvidence().isDisabledRequest() || !properties.getEvidence().isDisabledUpload()) {
            if (!dir.toFile().exists()) {
                if (!this.dir.toFile().mkdirs()) {
                    logger.error("Evidence Directory cannot create. dir = {}", dir.toAbsolutePath().toString());
                }
            }
        }
        this.contentExtension = contentExtension;
        this.properties = properties;
    }

    void start() {
        logger.info("Start.");
        logger.info("Evidence Dir : {}", dir.toAbsolutePath().toString());
    }

    void request(HttpServletRequest request, RequestEntity<InputStreamResource> requestEntity) throws IOException, ServletException {
        final EvidenceRequest evidenceRequest = new EvidenceRequest(request.getParameterMap(), requestEntity.getHeaders());
        logger.info("Request      : {}", objectMapperForLog.writeValueAsString(evidenceRequest));
        if (!properties.getEvidence().isDisabledRequest()) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(dir.toFile(), "request.json")))) {
                objectMapperForFile.writeValue(out, evidenceRequest);
            }
        }

        if (requestEntity.getBody() != null) {
            String body = StreamUtils.copyToString(requestEntity.getBody().getInputStream(), Charset.forName(request.getCharacterEncoding()));
            logger.info("Request body : {}", body);
            if (!properties.getEvidence().isDisabledRequest()) {
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(dir.toFile(), "body." + contentExtension)))) {
                    StreamUtils.copy(body, StandardCharsets.UTF_8, out);
                }
            }
        }

        if (request instanceof MultipartHttpServletRequest) {
            int index = 1;
            for (Part part : request.getParts()) {
                String fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                String saveFileName = String.format("uploadFile_%02d_%s", index, fileName);
                File saveFile = new File(dir.toFile(), saveFileName);
                UploadFile uploadFile = new UploadFile(part, saveFileName);
                logger.info("Upload file  : {}", objectMapperForLog.writeValueAsString(uploadFile));
                if (!properties.getEvidence().isDisabledUpload()) {
                    try (InputStream in = part.getInputStream(); OutputStream out = new BufferedOutputStream(new FileOutputStream(saveFile))) {
                        FileCopyUtils.copy(in, out);
                    }
                }
                index++;
            }
        }
    }

    void response(ResponseEntity<Object> responseEntity) throws JsonProcessingException {
        EvidenceResponse evidenceResponse = new EvidenceResponse(responseEntity.getStatusCode(), responseEntity.getHeaders());
        logger.info("Response     : {}", objectMapperForLog.writeValueAsString(evidenceResponse));
    }


    void end() {
        logger.info("End.");
    }

    @Data
    private static class UploadFile implements Serializable {
        private final String saveFileName;
        private final long size;
        private final HttpHeaders headers;

        private UploadFile(Part part, String saveFileName) {
            this.saveFileName = saveFileName;
            this.size = part.getSize();
            HttpHeaders headers = new HttpHeaders();
            part.getHeaderNames().forEach(e -> headers.put(e, new ArrayList<>(part.getHeaders(e))));
            this.headers = HttpHeaders.readOnlyHttpHeaders(headers);
        }
    }

    @RequiredArgsConstructor
    @Data
    private static class EvidenceRequest {
        private final Map<String, String[]> parameters;
        private final HttpHeaders headers;
    }

    @RequiredArgsConstructor
    @Data
    private static class EvidenceResponse {
        private final HttpStatus httpStatus;
        private final HttpHeaders headers;
    }

}
