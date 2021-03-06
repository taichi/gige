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
package io.gige.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import io.gige.Unit;

/** @author taichi */
public class OnTheFlyUnit implements Unit {
  final String className;

  final CharSequence source;

  public OnTheFlyUnit(String className, CharSequence source) {
    this.className = className;
    this.source = source;
  }

  @Override
  public JavaFileObject apply(JavaFileManager t) {
    try {
      final JavaFileObject obj =
          t.getJavaFileForOutput(StandardLocation.SOURCE_OUTPUT, this.className, Kind.SOURCE, null);
      try (Writer w = obj.openWriter()) {
        PrintWriter pw = new PrintWriter(w);
        pw.print(this.source.toString());
      }
      return obj;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
