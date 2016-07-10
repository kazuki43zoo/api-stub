package com.kazuki43zoo.manager.api;

import lombok.Data;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
class ApiSearchForm implements Serializable {
    private static final long serialVersionUID = 1L;
    private String path;
    @Pattern(regexp = "|GET|POST|PUT|DELETE|PATCH", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String method;
    private String description;
}
