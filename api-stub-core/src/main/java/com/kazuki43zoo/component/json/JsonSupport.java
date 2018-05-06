package com.kazuki43zoo.component.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kazuki43zoo.domain.model.Api;
import com.kazuki43zoo.domain.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JsonSupport {

    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public <T> List<T> jsonToList(String json) {
        try {
            return (List<T>) objectMapper.readValue(json, List.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String listToJson(List<?> list) throws JsonProcessingException {
        return objectMapper.writeValueAsString(list);
    }

    public List<Api> jsonToApiList(InputStream json) throws IOException {
        return Arrays.asList(objectMapper.readValue(json, Api[].class));
    }

    public List<ApiResponse> jsonToApiResponseList(InputStream json) throws IOException {
        return Arrays.asList(objectMapper.readValue(json, ApiResponse[].class));
    }

}
