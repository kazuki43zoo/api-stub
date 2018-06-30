/*
 *    Copyright 2016-2017 the original author or authors.
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
package com.kazuki43zoo.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Data
public class ApiResponse implements Serializable {
  private static final long serialVersionUID = 1L;
  private int id;
  private int subId;
  private String path;
  private String method;
  private String dataKey;
  private Integer statusCode;
  private String header;
  @JsonDeserialize(using = TextJsonDeserializer.class)
  @JsonSerialize(using = TextJsonSerializer.class)
  private transient InputStream body;
  private String bodyEditorMode;
  @JsonDeserialize(using = Base64JsonDeserializer.class)
  @JsonSerialize(using = Base64JsonSerializer.class)
  private transient InputStream attachmentFile;
  private String fileName;
  private Long waitingMsec;
  private String description;
  @JsonIgnore
  private LocalDateTime createdAt;
  @JsonIgnore
  private int historyNumber;

  private static class Base64JsonSerializer extends JsonSerializer<InputStream> {
    @Override
    public void serialize(InputStream value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeBinary(StreamUtils.copyToByteArray(value));
    }
  }

  private static class Base64JsonDeserializer extends JsonDeserializer<InputStream> {
    @Override
    public InputStream deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      return new ByteArrayInputStream(p.getBinaryValue());
    }
  }

  private static class TextJsonSerializer extends JsonSerializer<InputStream> {
    @Override
    public void serialize(InputStream value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeString(StreamUtils.copyToString(value, StandardCharsets.UTF_8));
    }
  }

  private static class TextJsonDeserializer extends JsonDeserializer<InputStream> {
    @Override
    public InputStream deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      String value = p.getValueAsString();
      if (value != null) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
      } else {
        return null;
      }
    }
  }

}
