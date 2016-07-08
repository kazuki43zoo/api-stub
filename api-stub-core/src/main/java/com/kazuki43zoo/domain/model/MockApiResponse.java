package com.kazuki43zoo.domain.model;

import lombok.Data;

import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MockApiResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int subId;
    private String path;
    private String method;
    private Integer statusCode;
    private String header;
    private InputStream body;
    private String bodyEditorMode;
    private InputStream attachmentFile;
    private String fileName;
    private Long waitingMsec;
    private String description;
    private LocalDateTime createdAt;

}
