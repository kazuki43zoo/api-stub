package com.kazuki43zoo.manager.api;

import com.kazuki43zoo.component.validation.HttpMethod;
import lombok.Data;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
class ApiSearchForm implements Serializable {
    private static final long serialVersionUID = 1L;
    private String path;
    @HttpMethod
    private String method;
    private String description;
}
