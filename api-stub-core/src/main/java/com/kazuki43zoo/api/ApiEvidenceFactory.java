package com.kazuki43zoo.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.accept.ContentNegotiationManager;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Optional;

@Component
class ApiEvidenceFactory {

    @Autowired
    ApiStubProperties properties;

    @Autowired
    ContentNegotiationManager contentNegotiationManager;


    ApiEvidence create(HttpServletRequest request, String dataKey, String correlationId) {

        String contentExtension = Optional.ofNullable(request.getContentType())
                .map(contentType -> contentNegotiationManager.resolveFileExtensions(MediaType.parseMediaType(contentType)))
                .orElseGet(ArrayList::new).stream().findFirst().orElse("txt");

        return new ApiEvidence(properties, request.getMethod(), request.getServletPath(), dataKey, correlationId, contentExtension);
    }

}
