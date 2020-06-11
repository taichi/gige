/*
 * Copyright 2014 - 2015 SATO taichi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.gige;

import java.util.function.Function;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import io.gige.internal.FileUnit;
import io.gige.internal.OnTheFlyUnit;

/** @author taichi */
public interface Unit extends Function<StandardJavaFileManager, JavaFileObject> {

  static Unit of(String className) {
    return new FileUnit(className);
  }

  static Unit of(Class<?> clazz) {
    return of(clazz.getCanonicalName());
  }

  static Unit of(String className, CharSequence source) {
    return new OnTheFlyUnit(className, source);
  }

  static Unit of(Class<?> clazz, CharSequence source) {
    return new OnTheFlyUnit(clazz.getCanonicalName(), source);
  }
}
