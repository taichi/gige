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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

/**
 * @author taichi
 */
public class AnnotationProcessingEnv extends BaseProcessingEnvImpl {

	protected Locale locale = Locale.getDefault();

	public AnnotationProcessingEnv(JavaFileManager fileManager,
			DiagnosticListener<? super JavaFileObject> diagnosticListener,
			Iterable<String> options,
			Compiler compiler) {
		super();
		this._filer = new FilerImpl(fileManager, this);
		this._messager = new MessagerImpl(this, diagnosticListener);
		this._processorOptions = parseProcessorOptions(options);
		this._compiler = compiler;
		this._elementUtils = new HackElements(this);
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public Locale getLocale() {
		return this.locale;
	}

	protected Map<String, String> parseProcessorOptions(Iterable<String> args) {
		Map<String, String> options = new LinkedHashMap<String, String>();
		for (String arg : args) {
			if (!arg.startsWith("-A")) { //$NON-NLS-1$
				continue;
			}
			int equals = arg.indexOf('=');
			if (equals == 2) {
				// option begins "-A=" - not valid
				Exception e = new IllegalArgumentException(
						"-A option must have a key before the equals sign"); //$NON-NLS-1$
				throw new AbortCompilation(null, e);
			}
			if (equals == arg.length() - 1) {
				// option ends with "=" - not valid
				options.put(arg.substring(2, equals), null);
			} else if (equals == -1) {
				// no value
				options.put(arg.substring(2), null);
			} else {
				// value and key
				options.put(arg.substring(2, equals),
						arg.substring(equals + 1));
			}
		}
		return options;
	}
}
