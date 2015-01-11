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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.gige.internal.CompositeDiagnosticListener;
import io.gige.internal.ResourceProxyJavaFileManager;

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
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.junit.Assert;

/**
 * @author taichi
 */
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
		assertNotNull(provider);
		this.provider = provider;
		classOutputs = sourceOutputs = Arrays.asList(new File(".gige", provider
				.toString()));
	}

	public CompilerContext set(Processor... processors) {
		assertNotNull(processors);
		assertTrue(0 < processors.length);
		this.processors = Arrays.asList(processors);
		return this;
	}

	public CompilerContext set(DiagnosticListener<JavaFileObject> listener) {
		assertNotNull(listener);
		this.diagnosticListener = listener;
		return this;
	}

	public CompilerContext set(Unit... units) {
		assertNotNull(units);
		assertTrue(0 < units.length);
		this.units = Arrays.asList(units);
		return this;
	}

	public CompilerContext setUnits(String... units) {
		assertNotNull(units);
		assertTrue(0 < units.length);
		this.units = Stream.of(units).filter(s -> s.isEmpty() == false)
				.map(Unit::of).collect(Collectors.toList());
		assertFalse(this.units.isEmpty());
		return this;
	}

	public CompilerContext setUnits(Class<?>... units) {
		assertNotNull(units);
		assertTrue(0 < units.length);
		this.units = Stream.of(units).map(Unit::of)
				.collect(Collectors.toList());
		assertFalse(this.units.isEmpty());
		return this;
	}

	public CompilerContext setOptions(Iterable<String> options) {
		assertNotNull(options);
		this.options = options;
		return this;
	}

	public CompilerContext set(Locale locale) {
		Assert.assertNotNull(locale);
		this.locale = locale;
		return this;
	}

	public CompilerContext setLocale(String locale) {
		assertNotNull(locale);
		assertFalse(locale.isEmpty());
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
		assertNotNull(charset);
		assertFalse(charset.isEmpty());
		this.charset = Charset.forName(charset);
		return this;
	}

	public Charset getCharset() {
		return this.charset == null ? Charset.defaultCharset() : this.charset;
	}

	public CompilerContext set(Writer writer) {
		assertNotNull(writer);
		this.out = writer;
		return this;
	}

	public Writer getOut() {
		return this.out;
	}

	protected List<File> toFiles(String... paths) {
		List<File> files = Stream.of(paths).filter(s -> s.isEmpty() == false)
				.map(File::new).collect(Collectors.toList());
		assertTrue(0 < files.size());
		return files;
	}

	public CompilerContext setSourcePath(String... sourcepath) {
		assertNotNull(sourcepath);
		assertTrue(0 < sourcepath.length);
		this.sourcePaths = toFiles(sourcepath);
		return this;
	}

	public CompilerContext setSourcePath(File... sourcepath) {
		assertNotNull(sourcepath);
		assertTrue(0 < sourcepath.length);
		this.sourcePaths = Arrays.asList(sourcepath);
		return this;
	}

	public CompilerContext setClassOutputs(String... outputs) {
		assertNotNull(outputs);
		assertTrue(0 < outputs.length);
		this.classOutputs = toFiles(outputs);
		return this;
	}

	public CompilerContext setClassOutputs(File... outputs) {
		assertNotNull(outputs);
		assertTrue(0 < outputs.length);
		this.classOutputs = Arrays.asList(outputs);
		return this;
	}

	public CompilerContext setSourceOutputs(String... outputs) {
		assertNotNull(outputs);
		assertTrue(0 < outputs.length);
		this.sourceOutputs = toFiles(outputs);
		return this;
	}

	public CompilerContext setSourceOutputs(File... outputs) {
		assertNotNull(outputs);
		assertTrue(0 < outputs.length);
		this.sourceOutputs = Arrays.asList(outputs);
		return this;
	}

	public CompilationResult compile() throws IOException {
		assertFalse(this.units.isEmpty());

		JavaCompiler compiler = this.provider.get();
		CompositeDiagnosticListener dl = new CompositeDiagnosticListener(
				this.diagnosticListener);

		StandardJavaFileManager manager = compiler.getStandardFileManager(dl,
				getLocale(), getCharset());
		manager.setLocation(StandardLocation.SOURCE_PATH, this.sourcePaths);

		Stream.of(classOutputs, sourceOutputs).flatMap(List::stream)
				.filter(f -> f.exists() == false).forEach(f -> f.mkdirs());
		manager.setLocation(StandardLocation.CLASS_OUTPUT, classOutputs);
		manager.setLocation(StandardLocation.SOURCE_OUTPUT, sourceOutputs);
		this.managers.add(manager);

		CompilationTask task = compiler.getTask(this.getOut(), wrap(manager),
				dl, this.options, Collections.emptyList(), this.units.stream()
						.map(t -> t.apply(manager))
						.collect(Collectors.toList()));
		task.setLocale(getLocale());

		return newResult(dl.getDiagnostics(), manager, processors, task);
	}

	protected JavaFileManager wrap(StandardJavaFileManager manager) {
		// emulate auto resource copying.
		return new ResourceProxyJavaFileManager(manager);
	}

	protected CompilationResult newResult(
			List<Diagnostic<? extends JavaFileObject>> storage,
			StandardJavaFileManager manager, List<Processor> processors,
			CompilationTask task) {
		List<Processor> list = new ArrayList<>();
		EnvProcessor env = new EnvProcessor();
		list.add(env);
		list.addAll(this.processors);
		task.setProcessors(list);

		boolean success = task.call();

		return new CompilationResult(success, manager, storage,
				env.processingEnvironment);

	}

	@SupportedAnnotationTypes("*")
	public class EnvProcessor extends AbstractProcessor {

		ProcessingEnvironment processingEnvironment;

		@Override
		public SourceVersion getSupportedSourceVersion() {
			return SourceVersion.latest();
		}

		@Override
		public void init(final ProcessingEnvironment processingEnvironment) {
			super.init(processingEnvironment);
			this.processingEnvironment = processingEnvironment;
		}

		@Override
		public boolean process(final Set<? extends TypeElement> annotations,
				final RoundEnvironment roundEnv) {
			return false;
		}
	}

	@Override
	public void close() throws Exception {
		for (StandardJavaFileManager m : managers) {
			m.close();
		}
		this.managers.clear();
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " "
				+ Objects.toString(this.provider);
	}

}
