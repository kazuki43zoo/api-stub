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
package com.kazuki43zoo.screen.response;

import com.kazuki43zoo.component.validation.HttpMethod;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
class ApiResponseForm implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotEmpty
    @Size(max = 256)
    private String path;
    @NotEmpty
    @HttpMethod
    private String method;
    private String dataKey;
    @Min(100)
    @Max(599)
    private Integer statusCode = 200;
    private String header = "Content-Type: application/json";
    private String body;
    private String bodyEditorMode;
    private transient MultipartFile file;
    private boolean deleteFile;
    private Long waitingMsec;
    private String description;
    private boolean saveHistory;
}
