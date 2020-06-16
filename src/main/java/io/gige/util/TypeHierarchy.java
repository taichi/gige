/*
 * Copyright 2015 SATO taichi
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
package io.gige.util;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

/** @author taichi */
public class TypeHierarchy implements Spliterator<TypeElement> {

  public static Stream<TypeElement> of(ProcessingEnvironment env, TypeElement element) {
    return StreamSupport.stream(new TypeHierarchy(env, element), false);
  }

  final ProcessingEnvironment env;

  TypeElement current;

  public TypeHierarchy(ProcessingEnvironment env, TypeElement element) {
    this.env = env;
    this.current = element;
  }

  @Override
  public boolean tryAdvance(Consumer<? super TypeElement> action) {
    action.accept(this.current);
    this.current = GigeTypes.to(this.env, this.current.getSuperclass());
    return this.current != null && this.current.asType().getKind() != TypeKind.NONE;
  }

  @Override
  public Spliterator<TypeElement> trySplit() {
    return null;
  }

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return ORDERED | DISTINCT | NONNULL;
  }
}
