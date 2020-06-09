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

import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/** @author taichi */
public class CompositeDiagnosticListener implements DiagnosticListener<JavaFileObject> {

  final List<DiagnosticListener<JavaFileObject>> listeners;

  final DiagnosticCollector<JavaFileObject> storage = new DiagnosticCollector<>();

  public CompositeDiagnosticListener(DiagnosticListener<JavaFileObject> delegate) {
    this.listeners = Arrays.asList(this.storage, orElse(delegate));
  }

  protected DiagnosticListener<JavaFileObject> orElse(DiagnosticListener<JavaFileObject> delegate) {
    return delegate == null ? t -> {} : delegate;
  }

  @Override
  public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
    listeners.forEach(dl -> dl.report(diagnostic));
  }

  public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
    return this.storage.getDiagnostics();
  }
}
