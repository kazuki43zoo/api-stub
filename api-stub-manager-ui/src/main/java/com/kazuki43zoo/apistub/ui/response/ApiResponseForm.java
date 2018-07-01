/*
 *    Copyright 2016-2018 the original author or authors.
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
package com.kazuki43zoo.apistub.ui.response;

import com.kazuki43zoo.apistub.ui.component.validation.HttpMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class ApiResponseForm implements Serializable {
  private static final long serialVersionUID = 1L;
  @NotEmpty
  @Size(max = 256)
  private String path;
  @NotEmpty
  @HttpMethod
  private String method;
  private List<@NotEmpty @Size(max = 350) String> dataKeys = new ArrayList<>();
  @NotNull
  @Min(0)
  @Max(999)
  private Integer statusCode = 200;
  private String header = "Content-Type: application/json";
  private String body;
  private String bodyEditorMode;
  private transient MultipartFile file;
  private boolean deleteFile;
  private Long waitingMsec;
  private String description;
  private boolean saveHistory;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public List<String> getDataKeys() {
    return dataKeys;
  }

  public void setDataKeys(List<String> dataKeys) {
    this.dataKeys = dataKeys;
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getBodyEditorMode() {
    return bodyEditorMode;
  }

  public void setBodyEditorMode(String bodyEditorMode) {
    this.bodyEditorMode = bodyEditorMode;
  }

  public MultipartFile getFile() {
    return file;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
  }

  public boolean isDeleteFile() {
    return deleteFile;
  }

  public void setDeleteFile(boolean deleteFile) {
    this.deleteFile = deleteFile;
  }

  public Long getWaitingMsec() {
    return waitingMsec;
  }

  public void setWaitingMsec(Long waitingMsec) {
    this.waitingMsec = waitingMsec;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isSaveHistory() {
    return saveHistory;
  }

  public void setSaveHistory(boolean saveHistory) {
    this.saveHistory = saveHistory;
  }
}
