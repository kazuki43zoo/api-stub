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
package com.kazuki43zoo.apistub.api.util;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {

  private final Supplier<T> objectSupplier;
  private T object;

  public static <T> Lazy<T> of(Supplier<T> objectSupplier) {
    return new Lazy<>(objectSupplier);
  }

  private Lazy(Supplier<T> objectSupplier) {
    this.objectSupplier = objectSupplier;
  }

  public T get() {
    if (object == null) {
      object = objectSupplier.get();
    }
    return object;
  }

}
