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
package com.kazuki43zoo.component.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageCode {
  /** */
  UNDEFINED("msg.undefined"),
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
  , DATA_HAS_BEEN_RESTORED("msg.dataHasBeenRestored")
  /** */
  , DATA_HAS_BEEN_IMPORTED("msg.dataHasBeenImported")
  /** */
  , ALL_DATA_HAS_NOT_BEEN_IMPORTED("msg.allDataHasNotBeenImported")
  /** */
  , PARTIALLY_DATA_HAS_NOT_BEEN_IMPORTED("msg.partiallyDataHasNotBeenImported")
  /** */
  , KEYED_RESPONSE_EXISTS("msg.keyedResponseExists")
  /** */
  , IMPORT_FILE_NOT_SELECTED("msg.importFileNotSelected")
  /** */
  , IMPORT_FILE_EMPTY("msg.importFileEmpty")
  /** */
  , IMPORT_DATA_EMPTY("msg.importDataEmpty")
  /** */
  , INVALID_JSON("msg.invalidJson");

  private final String value;

}
