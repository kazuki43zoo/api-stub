package com.kazuki43zoo.component.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InfoMessage {
    private final MessageCode code;
    private final Object args;
}
