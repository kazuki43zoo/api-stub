package com.kazuki43zoo.domain.model;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.StringJoiner;

public enum KeyGeneratingStrategy {

    FIRST {
        @Override
        public String generate(List<String> keys) {
            if (keys == null || keys.isEmpty()) {
                return null;
            }
            return keys.stream().filter(StringUtils::hasLength).findFirst().orElse(null);
        }
    },
    ALL {
        @Override
        public String generate(List<String> keys) {
            if (keys == null || keys.isEmpty()) {
                return null;
            }
            StringJoiner stringJoiner = new StringJoiner("/");
            keys.forEach(stringJoiner::add);
            return stringJoiner.toString();
        }
    };

    public abstract String generate(List<String> values);

}
