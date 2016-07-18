package com.kazuki43zoo.component.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorMessage {
    private final MessageCode code;
    private final Object args;
}
