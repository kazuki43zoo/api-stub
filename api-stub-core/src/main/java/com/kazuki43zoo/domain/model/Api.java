package com.kazuki43zoo.domain.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Api implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String path;
    private String method;
    private String keyExtractor;
    private KeyGeneratingStrategy keyGeneratingStrategy;
    private String expressions;
    private String description;
}
