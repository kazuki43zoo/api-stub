package com.kazuki43zoo.domain;

import lombok.Data;

import java.io.InputStream;
import java.io.Serializable;

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
    private InputStream attachmentFile;
    private String fileName;
    private String description;
}
