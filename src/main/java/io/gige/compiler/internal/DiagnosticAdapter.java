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

import java.io.File;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.apt.util.EclipseFileObject;

/**
 * @author taichi
 */
public class DiagnosticAdapter implements Diagnostic<JavaFileObject> {

	final Kind kind;
	final IProblem problem;

	public DiagnosticAdapter(Kind kind, IProblem problem) {
		this.kind = kind;
		this.problem = problem;
	}

	@Override
	public javax.tools.Diagnostic.Kind getKind() {
		return this.kind;
	}

	@Override
	public JavaFileObject getSource() {
		char[] filename = this.problem.getOriginatingFileName();
		if (filename == null) {
			return null;
		}
		File file = new File(new String(filename));
		if (file.exists()) {
			return new EclipseFileObject(null, file.toURI(),
					JavaFileObject.Kind.SOURCE, null);
		}
		return null;
	}

	@Override
	public long getPosition() {
		return problem.getSourceStart();
	}

	@Override
	public long getStartPosition() {
		return getPosition();
	}

	@Override
	public long getEndPosition() {
		return problem.getSourceEnd();
	}

	@Override
	public long getLineNumber() {
		return this.problem.getSourceLineNumber();
	}

	@Override
	public long getColumnNumber() {
		return NOPOS;
	}

	@Override
	public String getCode() {
		return null;
	}

	@Override
	public String getMessage(Locale locale) {
		return this.problem.getMessage();
	}

	@Override
	public String toString() {
		return this.problem.toString();
	}
}
