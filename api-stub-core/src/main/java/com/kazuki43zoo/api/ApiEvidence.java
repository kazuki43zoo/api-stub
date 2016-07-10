package com.kazuki43zoo.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
    private final Logger logger;
    private final String contentExtension;
    private final ApiStubProperties properties;

    ApiEvidence(ApiStubProperties properties, String method, String path, String dataKey, String correlationId, String contentExtension) {
        MDC.put(properties.getCorrelationIdKey(), correlationId);
        this.dir = Paths.get(properties.getEvidence().getDir(),
                path, (dataKey != null ? dataKey : ""), method, LocalDateTime.now().format(DIR_NAME_DATE_TIME_FORMAT) + "_" + correlationId);
        this.logger = LoggerFactory.getLogger(method + " " + path + (dataKey != null ? (" dataKey=" + dataKey) : ""));
        this.contentExtension = contentExtension;
        this.properties = properties;
    }

    void start() {
        info("Start.");
        if (!properties.getEvidence().isDisabledRequest() || !properties.getEvidence().isDisabledUpload()) {
            if (!dir.toFile().exists()) {
                if (!this.dir.toFile().mkdirs()) {
                    error("Evidence Directory cannot create. dir = {}", dir.toAbsolutePath().toString());
                }
            }
            info("Evidence Dir : {}", dir.toAbsolutePath().toString());
        }
    }

    void request(HttpServletRequest request, RequestEntity<String> requestEntity) throws IOException, ServletException {
        final EvidenceRequest evidenceRequest = new EvidenceRequest(request.getParameterMap(), requestEntity.getHeaders());
        info("Request      : {}", objectMapperForLog.writeValueAsString(evidenceRequest));
        if (!properties.getEvidence().isDisabledRequest()) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(dir.toFile(), "request.json")))) {
                objectMapperForFile.writeValue(out, evidenceRequest);
            }
        }

        if (requestEntity.getBody() != null) {
            final String body = requestEntity.getBody();
            info("Request body : {}", body);
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
                info("Upload file  : {}", objectMapperForLog.writeValueAsString(uploadFile));
                if (!properties.getEvidence().isDisabledUpload()) {
                    try (InputStream in = part.getInputStream(); OutputStream out = new BufferedOutputStream(new FileOutputStream(saveFile))) {
                        FileCopyUtils.copy(in, out);
                    }
                }
                index++;
            }
        }
    }

    void response(ResponseEntity<InputStreamResource> responseEntity) throws JsonProcessingException {
        EvidenceResponse evidenceResponse = new EvidenceResponse(responseEntity.getStatusCode(), responseEntity.getHeaders());
        info("Response     : {}", objectMapperForLog.writeValueAsString(evidenceResponse));
    }


    void end() {
        info("End.");
        MDC.clear();
    }

    void error(String format, Object... args) {
        logger.error(format, args);
    }

    void warn(String format, Object... args) {
        logger.warn(format, args);
    }

    void info(String format, Object... args) {
        logger.info(format, args);
    }

    @Data
    private static class UploadFile implements Serializable {
        private static final long serialVersionUID = 1L;
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

    @Data
    private static class EvidenceRequest {
        private final Map<String, String[]> parameters;
        private final HttpHeaders headers;
    }

    @Data
    private static class EvidenceResponse {
        private final HttpStatus httpStatus;
        private final HttpHeaders headers;
    }

}
