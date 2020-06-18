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
package io.gige.compiler.internal;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/** @author taichi */
public class DiagnosticListenerWrapper implements DiagnosticListener<JavaFileObject> {

  final DiagnosticListener<? super JavaFileObject> delegate;
  boolean hasError = false;

  public DiagnosticListenerWrapper(DiagnosticListener<? super JavaFileObject> delegate) {
    super();
    this.delegate = delegate;
  }

  @Override
  public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
    if (diagnostic.getKind().equals(Kind.ERROR)) {
      this.hasError = true;
    }
    this.delegate.report(diagnostic);
  }

  public boolean hasError() {
    return this.hasError;
  }

  public boolean succeed() {
    return this.hasError == false;
  }
}
