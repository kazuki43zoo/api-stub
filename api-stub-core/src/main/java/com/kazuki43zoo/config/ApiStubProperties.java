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
package com.kazuki43zoo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "api")
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
    private Evidence evidence = new Evidence();

    @NestedConfigurationProperty
    private Proxy proxy = new Proxy();

    @Setter
    @Getter
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

    }

    @Setter
    @Getter
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
    }

}