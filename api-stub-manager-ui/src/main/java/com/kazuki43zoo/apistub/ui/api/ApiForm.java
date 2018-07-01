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
package com.kazuki43zoo.apistub.ui.api;

import com.kazuki43zoo.apistub.domain.model.KeyGeneratingStrategy;
import com.kazuki43zoo.apistub.ui.component.validation.HttpMethod;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

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

  public String getKeyExtractor() {
    return keyExtractor;
  }

  public void setKeyExtractor(String keyExtractor) {
    this.keyExtractor = keyExtractor;
  }

  public KeyGeneratingStrategy getKeyGeneratingStrategy() {
    return keyGeneratingStrategy;
  }

  public void setKeyGeneratingStrategy(KeyGeneratingStrategy keyGeneratingStrategy) {
    this.keyGeneratingStrategy = keyGeneratingStrategy;
  }

  public List<String> getExpressions() {
    return expressions;
  }

  public void setExpressions(List<String> expressions) {
    this.expressions = expressions;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Proxy getProxy() {
    return proxy;
  }

  public void setProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  public static class Proxy implements Serializable {
    private static final long serialVersionUID = 1L;
    private Boolean enabled;
    private String url;
    private Boolean capturing;

    public Boolean getEnabled() {
      return enabled;
    }

    public void setEnabled(Boolean enabled) {
      this.enabled = enabled;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public Boolean getCapturing() {
      return capturing;
    }

    public void setCapturing(Boolean capturing) {
      this.capturing = capturing;
    }
  }

}
