package com.kazuki43zoo.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "api")
class ApiStubProperties {

    /**
     * key for correlation id
     */
    private String correlationIdKey = "x-correlation-id";

    @NestedConfigurationProperty
    private Evidence evidence = new Evidence();

    @Setter
    @Getter
    static class Evidence {
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

}