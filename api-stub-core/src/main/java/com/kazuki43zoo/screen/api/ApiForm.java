/**
 *    Copyright 2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
    private Proxy proxy = new Proxy();

    @Data
    public static class Proxy implements Serializable{
        private static final long serialVersionUID = 1L;
        private Boolean enabled;
        private String url;
        private Boolean capturing;
    }

}
