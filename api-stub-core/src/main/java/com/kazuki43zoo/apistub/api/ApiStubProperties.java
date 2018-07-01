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
package com.kazuki43zoo.apistub.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.thymeleaf.templatemode.TemplateMode;

@Component
@ConfigurationProperties(prefix = "apistub.core")
public class ApiStubProperties {

  /**
   * key for correlation id
   */
  private String correlationIdKey = "x-correlation-id";

  /**
   * Root path for API
   */
  private String rootPath = "/api";

  @NestedConfigurationProperty
  private Response response = new Response();

  @NestedConfigurationProperty
  private Evidence evidence = new Evidence();

  @NestedConfigurationProperty
  private Proxy proxy = new Proxy();

  public String getCorrelationIdKey() {
    return correlationIdKey;
  }

  public void setCorrelationIdKey(String correlationIdKey) {
    this.correlationIdKey = correlationIdKey;
  }

  public String getRootPath() {
    return rootPath;
  }

  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  public Response getResponse() {
    return response;
  }

  public void setResponse(Response response) {
    this.response = response;
  }

  public Evidence getEvidence() {
    return evidence;
  }

  public void setEvidence(Evidence evidence) {
    this.evidence = evidence;
  }

  public Proxy getProxy() {
    return proxy;
  }

  public void setProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  public static class Response {
    /**
     * HTTP status to respond when a mock response not found
     */
    private HttpStatus httpStatusForMockNotFound = HttpStatus.NOT_FOUND;

    @NestedConfigurationProperty
    private Template template = new Template();

    public HttpStatus getHttpStatusForMockNotFound() {
      return httpStatusForMockNotFound;
    }

    public void setHttpStatusForMockNotFound(HttpStatus httpStatusForMockNotFound) {
      this.httpStatusForMockNotFound = httpStatusForMockNotFound;
    }

    public Template getTemplate() {
      return template;
    }

    public void setTemplate(Template template) {
      this.template = template;
    }

    public static class Template {
      /**
       * Flag for indicating to disable template feature.
       */
      private boolean disabled = false;

      /**
       * Template mode.
       */
      private TemplateMode mode = TemplateMode.TEXT;

      /**
       * Flag for indicating to enable SpEL compiler.
       */
      private boolean enabledSpelCompiler = true;

      public boolean isDisabled() {
        return disabled;
      }

      public void setDisabled(boolean disabled) {
        this.disabled = disabled;
      }

      public TemplateMode getMode() {
        return mode;
      }

      public void setMode(TemplateMode mode) {
        this.mode = mode;
      }

      public boolean isEnabledSpelCompiler() {
        return enabledSpelCompiler;
      }

      public void setEnabledSpelCompiler(boolean enabledSpelCompiler) {
        this.enabledSpelCompiler = enabledSpelCompiler;
      }
    }

  }

  public static class Evidence {
    /**
     * Evidence directory.
     */
    private String dir = "evidence";

    /**
     * Disabled evidence.
     */
    private boolean disabledRequest = false;

    /**
     * Disabled evidence for upload.
     */
    private boolean disabledUpload = false;

    public String getDir() {
      return dir;
    }

    public void setDir(String dir) {
      this.dir = dir;
    }

    public boolean isDisabledRequest() {
      return disabledRequest;
    }

    public void setDisabledRequest(boolean disabledRequest) {
      this.disabledRequest = disabledRequest;
    }

    public boolean isDisabledUpload() {
      return disabledUpload;
    }

    public void setDisabledUpload(boolean disabledUpload) {
      this.disabledUpload = disabledUpload;
    }
  }

  public static class Proxy {
    /**
     * Disabled proxy.
     */
    private boolean defaultEnabled = false;
    /**
     * Disabled proxy capturing.
     */
    private boolean defaultCapturing = false;
    /**
     * Base url of proxy.
     */
    private String defaultUrl;

    public boolean isDefaultEnabled() {
      return defaultEnabled;
    }

    public void setDefaultEnabled(boolean defaultEnabled) {
      this.defaultEnabled = defaultEnabled;
    }

    public boolean isDefaultCapturing() {
      return defaultCapturing;
    }

    public void setDefaultCapturing(boolean defaultCapturing) {
      this.defaultCapturing = defaultCapturing;
    }

    public String getDefaultUrl() {
      return defaultUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
      this.defaultUrl = defaultUrl;
    }

  }

}