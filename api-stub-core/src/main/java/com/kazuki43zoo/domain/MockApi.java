package com.kazuki43zoo.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class MockApi implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String path;
    private String method;
    private String contentType;
    private String description;
}
