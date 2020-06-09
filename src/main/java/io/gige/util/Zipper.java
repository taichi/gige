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

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** @author taichi */
public class Zipper<L, R, T> implements Spliterator<T> {

  public static <L, R, T> Stream<T> of(Stream<L> left, Stream<R> right, BiFunction<L, R, T> fn) {
    return StreamSupport.stream(new Zipper<>(left.spliterator(), right.spliterator(), fn), false);
  }

  final Spliterator<L> lefts;
  final Spliterator<R> rights;
  final BiFunction<L, R, T> zipWith;

  public Zipper(Spliterator<L> lefts, Spliterator<R> rights, BiFunction<L, R, T> zipWith) {
    super();
    this.lefts = Objects.requireNonNull(lefts);
    this.rights = Objects.requireNonNull(rights);
    this.zipWith = Objects.requireNonNull(zipWith);
  }

  @Override
  public boolean tryAdvance(Consumer<? super T> action) {
    boolean[] rNext = {false};
    boolean lNext =
        this.lefts.tryAdvance(
            l -> {
              rNext[0] =
                  this.rights.tryAdvance(
                      r -> {
                        action.accept(this.zipWith.apply(l, r));
                      });
            });
    return lNext && rNext[0];
  }

  @Override
  public Spliterator<T> trySplit() {
    return new Zipper<>(this.lefts.trySplit(), this.rights.trySplit(), this.zipWith);
  }

  @Override
  public long estimateSize() {
    return Math.min(this.lefts.estimateSize(), this.rights.estimateSize());
  }

  @Override
  public int characteristics() {
    return lefts.characteristics() & rights.characteristics();
  }
}
