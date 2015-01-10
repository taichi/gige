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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;

/**
 * @author taichi
 */
public class CompilerRequestorImpl implements ICompilerRequestor {

	final StandardJavaFileManager manager;
	final DiagnosticListener<? super JavaFileObject> diagnosticListener;

	public CompilerRequestorImpl(StandardJavaFileManager manager,
			DiagnosticListener<? super JavaFileObject> diagnosticListener) {
		this.manager = manager;
		this.diagnosticListener = diagnosticListener;
	}

	@Override
	public void acceptResult(CompilationResult result) {
		if (result.hasProblems()) {
			report(Kind.ERROR, result.getErrors());
		}
		if (result.hasTasks()) {
			report(Kind.NOTE, result.getTasks());
		}
		try {
			for (ClassFile cf : result.getClassFiles()) {
				String className = new String(cf.fileName());
				JavaFileObject obj = this.manager.getJavaFileForOutput(
						StandardLocation.CLASS_OUTPUT, className,
						javax.tools.JavaFileObject.Kind.CLASS, null);
				try (OutputStream out = obj.openOutputStream()) {
					out.write(cf.getBytes());
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected void report(Kind kind, CategorizedProblem[] problems) {
		Stream.of(problems).map(p -> new DiagnosticAdapter(kind, p))
				.forEach(diagnosticListener::report);
	}

}
