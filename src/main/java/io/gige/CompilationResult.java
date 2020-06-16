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

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

/** @author taichi */
public class CompilationResult {

  final boolean success;
  final List<Diagnostic<? extends JavaFileObject>> storage;

  public CompilationResult(
      boolean success,
      StandardJavaFileManager manager,
      List<Diagnostic<? extends JavaFileObject>> storage) {
    this.success = success;
    this.storage = storage;
  }

  public boolean success() {
    return this.success;
  };

  public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
    return this.storage;
  }
}
