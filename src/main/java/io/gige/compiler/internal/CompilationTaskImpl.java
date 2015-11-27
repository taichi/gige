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

import static java.util.stream.StreamSupport.stream;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.processing.Processor;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

/**
 * @author taichi
 */
public class CompilationTaskImpl implements CompilationTask {

	protected DefaultProblemFactory problemFactory;
	protected DiagnosticListenerWrapper errorTrapper;
	protected AnnotationProcessingEnv processingEnv;
	protected AnnotationProcessorManager processorManager;

	boolean called = false;
	Compiler compiler = null;
	ICompilationUnit[] targets;

	public void configure(PrintWriter out,
			JavaFileManager fileManager,
			StandardJavaFileManager standardManager,
			FileSystem environment,
			DiagnosticListener<? super JavaFileObject> diagnosticListener,
			Iterable<String> argv,
			Iterable<String> classes,
			Iterable<? extends JavaFileObject> compilationUnits) {

		this.problemFactory = new DefaultProblemFactory();
		this.errorTrapper = new DiagnosticListenerWrapper(diagnosticListener);

		Main main = parseOptions(out, argv, compilationUnits);
		CompilerOptions options = new CompilerOptions(main.options);
		options.verbose = main.verbose;
		ICompilerRequestor requestor = new CompilerRequestorImpl(
				standardManager, this.errorTrapper);
		this.compiler = new Compiler(environment, getHandlingPolicy(), options,
				requestor, this.problemFactory, out,
				null) {
			@Override
			public void reset() {
				// for OnTheFlyUnit
			}

			@Override
			public boolean shouldCleanup(int index) {
				return false;
			}
		};

		setTargets(compilationUnits);

		this.processingEnv = new AnnotationProcessingEnv(fileManager,
				errorTrapper, argv, compiler);
		this.processorManager = new AnnotationProcessorManager(
				this.processingEnv);
		this.processorManager.configure(argv);
		this.processorManager.setOut(out);
		this.processorManager.setErr(out);
		this.compiler.annotationProcessorManager = this.processorManager;
	}

	protected Main parseOptions(PrintWriter out,
			Iterable<String> argv,
			Iterable<? extends JavaFileObject> compilationUnits) {
		Map<String, String> defaults = new HashMap<>();
		defaults.put(CompilerOptions.OPTION_Compliance,
				CompilerOptions.VERSION_1_8);
		defaults.put(CompilerOptions.OPTION_Source,
				CompilerOptions.VERSION_1_8);
		defaults.put(CompilerOptions.OPTION_TargetPlatform,
				CompilerOptions.VERSION_1_8);

		Stream<String> files = Stream.concat(stream(argv.spliterator(), false),
				stream(compilationUnits.spliterator(), false)
						.map(JavaFileObject::toUri)
						.map(uri -> new File(uri).getAbsolutePath()));

		Main main = new Main(out, out, false, defaults, null);
		main.configure(files.toArray(String[]::new));
		return main;
	}

	protected void setTargets(
			Iterable<? extends JavaFileObject> compilationUnits) {
		this.targets = stream(compilationUnits.spliterator(), false)
				.filter(file -> file.getKind() == JavaFileObject.Kind.SOURCE)
				.map(file -> new CompilationUnit(null, file.getName(), null) {
					@Override
					public char[] getContents() {
						try {
							return file
									.getCharContent(true)
									.toString()
									.toCharArray();
						} catch (IOException e) {
							throw new AbortCompilationUnit(null, e, null);
						}
					}
				})
				.toArray(ICompilationUnit[]::new);
	}

	@Override
	public Boolean call() {
		if (called) {
			throw new IllegalStateException("This task has already been run");
		}
		this.compiler.compile(this.targets);
		return this.errorTrapper.succeed();
	}

	@Override
	public void setProcessors(Iterable<? extends Processor> processors) {
		this.processorManager
				.setProcessors(stream(processors.spliterator(), false)
						.toArray(Processor[]::new));
	}

	protected IErrorHandlingPolicy getHandlingPolicy() {
		return DefaultErrorHandlingPolicies.exitAfterAllProblems();
	}

	@Override
	public void setLocale(Locale locale) {
		this.problemFactory.setLocale(locale);
		this.processingEnv.setLocale(locale);
	}
}
