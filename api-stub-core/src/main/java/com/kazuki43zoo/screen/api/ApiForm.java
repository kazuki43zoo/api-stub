package com.kazuki43zoo.screen.api;

import com.kazuki43zoo.component.validation.HttpMethod;
import com.kazuki43zoo.domain.model.KeyGeneratingStrategy;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
class ApiForm implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotEmpty
    @Size(max = 256)
    private String path;
    @NotEmpty
    @HttpMethod
    private String method;
    private String keyExtractor;
    private KeyGeneratingStrategy keyGeneratingStrategy;
    private List<String> expressions;
    private String description;
}
