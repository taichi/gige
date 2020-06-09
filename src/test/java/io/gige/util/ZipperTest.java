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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

/** @author taichi */
public class ZipperTest {

  @Test
  public void same() {
    List<String> zipped =
        Zipper.of(
                Stream.of(1, 2, 3),
                Stream.of(10, 20, 30),
                (l, r) -> {
                  return l + " " + r;
                })
            .collect(Collectors.toList());
    assertEquals("1 10", zipped.get(0));
    assertEquals("2 20", zipped.get(1));
    assertEquals("3 30", zipped.get(2));
  }

  @Test
  public void leftMin() throws Exception {
    List<String> zipped =
        Zipper.of(
                Stream.of(1, 2),
                Stream.of(10, 20, 30),
                (l, r) -> {
                  return l + " " + r;
                })
            .collect(Collectors.toList());
    assertEquals("1 10", zipped.get(0));
    assertEquals("2 20", zipped.get(1));
  }

  @Test
  public void rightMin() throws Exception {
    List<String> zipped =
        Zipper.of(
                Stream.of(1, 2, 3),
                Stream.of(10, 20),
                (l, r) -> {
                  return l + " " + r;
                })
            .collect(Collectors.toList());
    assertEquals("1 10", zipped.get(0));
    assertEquals("2 20", zipped.get(1));
  }
}
