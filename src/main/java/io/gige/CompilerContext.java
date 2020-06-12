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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.Processor;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.junit.Assert;

import io.gige.internal.CompositeDiagnosticListener;
import io.gige.internal.ResourceProxyJavaFileManager;

/** @author taichi */
public class CompilerContext implements AutoCloseable {

  protected Locale locale;

  protected Charset charset = StandardCharsets.UTF_8;

  protected Writer out;

  protected Iterable<String> options = Collections.emptyList();

  protected List<File> sourcePaths = Collections.emptyList();

  protected List<File> classOutputs = Collections.emptyList();

  protected List<File> sourceOutputs = Collections.emptyList();

  protected List<Unit> units = Collections.emptyList();

  protected List<Processor> processors = Collections.emptyList();

  protected DiagnosticListener<JavaFileObject> diagnosticListener;

  protected final Supplier<JavaCompiler> provider;

  protected List<StandardJavaFileManager> managers = new ArrayList<>();

  public CompilerContext() {
    this(Compilers.Type.Standard);
  }

  public CompilerContext(Supplier<JavaCompiler> provider) {
    Assert.assertNotNull(provider);
    this.provider = provider;
    this.classOutputs = this.sourceOutputs = Arrays.asList(new File(".gige", provider.toString()));
  }

  public CompilerContext set(Processor... processors) {
    Assert.assertNotNull(processors);
    Assert.assertTrue(0 < processors.length);
    this.processors = Arrays.asList(processors);
    return this;
  }

  public CompilerContext set(DiagnosticListener<JavaFileObject> listener) {
    Assert.assertNotNull(listener);
    this.diagnosticListener = listener;
    return this;
  }

  public CompilerContext set(Unit... units) {
    Assert.assertNotNull(units);
    Assert.assertTrue(0 < units.length);
    this.units = Arrays.asList(units);
    return this;
  }

  public CompilerContext setUnits(String... units) {
    Assert.assertNotNull(units);
    Assert.assertTrue(0 < units.length);
    this.units =
        Stream.of(units)
            .filter(s -> s.isEmpty() == false)
            .map(Unit::of)
            .collect(Collectors.toList());
    Assert.assertFalse(this.units.isEmpty());
    return this;
  }

  public CompilerContext setUnits(Class<?>... units) {
    Assert.assertNotNull(units);
    Assert.assertTrue(0 < units.length);
    this.units = Stream.of(units).map(Unit::of).collect(Collectors.toList());
    Assert.assertFalse(this.units.isEmpty());
    return this;
  }

  public CompilerContext setOptions(Iterable<String> options) {
    Assert.assertNotNull(options);
    this.options = options;
    return this;
  }

  public CompilerContext set(Locale locale) {
    Assert.assertNotNull(locale);
    this.locale = locale;
    return this;
  }

  public CompilerContext setLocale(String locale) {
    Assert.assertNotNull(locale);
    Assert.assertFalse(locale.isEmpty());
    this.locale = new Locale(locale);
    return this;
  }

  public Locale getLocale() {
    return this.locale == null ? Locale.getDefault() : this.locale;
  }

  public CompilerContext set(Charset charset) {
    Assert.assertNotNull(charset);
    this.charset = charset;
    return this;
  }

  public CompilerContext setCharset(String charset) {
    Assert.assertNotNull(charset);
    Assert.assertFalse(charset.isEmpty());
    this.charset = Charset.forName(charset);
    return this;
  }

  public Charset getCharset() {
    return this.charset == null ? Charset.defaultCharset() : this.charset;
  }

  public CompilerContext set(Writer writer) {
    Assert.assertNotNull(writer);
    this.out = writer;
    return this;
  }

  public Writer getOut() {
    return this.out;
  }

  protected List<File> toFiles(String... paths) {
    List<File> files =
        Stream.of(paths)
            .filter(s -> s.isEmpty() == false)
            .map(File::new)
            .collect(Collectors.toList());
    Assert.assertTrue(0 < files.size());
    return files;
  }

  public CompilerContext setSourcePath(String... sourcepath) {
    Assert.assertNotNull(sourcepath);
    Assert.assertTrue(0 < sourcepath.length);
    this.sourcePaths = this.toFiles(sourcepath);
    return this;
  }

  public CompilerContext setSourcePath(File... sourcepath) {
    Assert.assertNotNull(sourcepath);
    Assert.assertTrue(0 < sourcepath.length);
    this.sourcePaths = Arrays.asList(sourcepath);
    return this;
  }

  public CompilerContext setClassOutputs(String... outputs) {
    Assert.assertNotNull(outputs);
    Assert.assertTrue(0 < outputs.length);
    this.classOutputs = this.toFiles(outputs);
    return this;
  }

  public CompilerContext setClassOutputs(File... outputs) {
    Assert.assertNotNull(outputs);
    Assert.assertTrue(0 < outputs.length);
    this.classOutputs = Arrays.asList(outputs);
    return this;
  }

  public CompilerContext setSourceOutputs(String... outputs) {
    Assert.assertNotNull(outputs);
    Assert.assertTrue(0 < outputs.length);
    this.sourceOutputs = this.toFiles(outputs);
    return this;
  }

  public CompilerContext setSourceOutputs(File... outputs) {
    Assert.assertNotNull(outputs);
    Assert.assertTrue(0 < outputs.length);
    this.sourceOutputs = Arrays.asList(outputs);
    return this;
  }

  public CompilationResult compile() throws IOException {
    return this.compile(ctx -> {});
  }

  public CompilationResult compile(AssertionBlock afterThat) throws IOException {
    Assert.assertFalse(this.units.isEmpty());

    JavaCompiler compiler = this.provider.get();
    var dl = new CompositeDiagnosticListener(this.diagnosticListener);

    StandardJavaFileManager manager =
        compiler.getStandardFileManager(dl, this.getLocale(), this.getCharset());
    manager.setLocation(StandardLocation.SOURCE_PATH, this.sourcePaths);

    Stream.of(this.classOutputs, this.sourceOutputs)
        .flatMap(List::stream)
        .filter(f -> f.exists() == false)
        .forEach(f -> f.mkdirs());
    manager.setLocation(StandardLocation.CLASS_OUTPUT, this.classOutputs);
    manager.setLocation(StandardLocation.SOURCE_OUTPUT, this.sourceOutputs);
    this.managers.add(manager);

    var list = new ArrayList<>(this.processors);
    var pros = new AssertionProcessor(manager, afterThat);
    list.add(pros);

    var task = this.newTask(compiler, dl, this.wrap(manager), this.map(manager, this.units), list);
    var result = new CompilationResult(task.call(), manager, dl.getDiagnostics());
    pros.rethrowOrNothing();

    return result;
  }

  CompilationTask newTask(
      JavaCompiler compiler,
      DiagnosticListener<JavaFileObject> dl,
      JavaFileManager manager,
      List<JavaFileObject> units,
      List<Processor> list) {
    CompilationTask task =
        compiler.getTask(this.getOut(), manager, dl, this.options, Collections.emptyList(), units);
    task.setLocale(this.getLocale());
    task.setProcessors(list);
    return task;
  }

  protected JavaFileManager wrap(StandardJavaFileManager manager) {
    // emulate auto resource copying.
    return new ResourceProxyJavaFileManager(manager);
  }

  protected List<JavaFileObject> map(JavaFileManager manager, List<Unit> units) {
    return units.stream().map(t -> t.apply(manager)).collect(Collectors.toList());
  }

  @Override
  public void close() throws Exception {
    for (StandardJavaFileManager m : this.managers) {
      m.close();
    }
    this.managers.clear();
  }

  @Override
  public String toString() {
    return this.getClass().getName() + " " + Objects.toString(this.provider);
  }
}
