package com.kazuki43zoo.manager.api;


import com.kazuki43zoo.domain.model.KeyGeneratingStrategy;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
class ApiForm implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotEmpty
    @Size(max = 256)
    private String path;
    @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String method;
    private String keyExtractor;
    private KeyGeneratingStrategy keyGeneratingStrategy;
    private List<String> expressions;
    private String description;

}
