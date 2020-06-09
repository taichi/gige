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
package io.gige;

import static org.junit.Assert.assertEquals;
import io.gige.util.Zipper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;

/** @author taichi */
public interface Asserts {

  static void assertEqualsByLine(final String expected, final String actual) {
    try (BufferedReader left = new BufferedReader(new StringReader(expected));
        BufferedReader right = new BufferedReader(new StringReader(actual))) {
      assertEqualsByLine(left, right);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  static void assertEqualsByLine(final BufferedReader expected, final BufferedReader actual)
      throws IOException {
    int[] lineNo = {0};
    Zipper.of(
        expected.lines(),
        actual.lines(),
        (l, r) -> {
          assertEquals("line:" + lineNo[0], l, r);
          lineNo[0]++;
          return false;
        });
  }
}
