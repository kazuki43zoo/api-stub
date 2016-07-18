package com.kazuki43zoo.component.message;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageCode {
    /** */
    DATA_NOT_FOUND("msg.dataNotFound")
    /** */
    , DATA_ALREADY_EXISTS("msg.dataAlreadyExists")
    /** */
    , DATA_HAS_BEEN_CREATED("msg.dataHasBeenCreated")
    /** */
    , DATA_HAS_BEEN_UPDATED("msg.dataHasBeenUpdated")
    /** */
    , DATA_HAS_BEEN_DELETED("msg.dataHasBeenDeleted")
    /** */
    , DATA_HAS_BEEN_RESTORED("msg.dataHasBeenRestored");

    private final String value;

}
