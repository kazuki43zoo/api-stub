package com.kazuki43zoo.screen.response;

import com.kazuki43zoo.component.validation.HttpMethod;
import lombok.Data;

import java.io.Serializable;

@Data
class ApiResponseSearchForm implements Serializable {
    private static final long serialVersionUID = 1L;
    private String path;
    @HttpMethod
    private String method;
    private String description;
}
