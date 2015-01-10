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

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import org.eclipse.jdt.internal.compiler.apt.dispatch.AptProblem;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseMessagerImpl;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;

/**
 * @author taichi
 */
public class MessagerImpl implements Messager {

	final BaseProcessingEnvImpl _processingEnv;
	final DiagnosticListener<? super JavaFileObject> diagnosticListener;

	public MessagerImpl(BaseProcessingEnvImpl env,
			DiagnosticListener<? super JavaFileObject> diagnosticListener) {
		this._processingEnv = env;
		this.diagnosticListener = diagnosticListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic
	 * .Kind, java.lang.CharSequence)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg) {
		printMessage(kind, msg, null, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic
	 * .Kind, java.lang.CharSequence, javax.lang.model.element.Element)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e) {
		printMessage(kind, msg, e, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic
	 * .Kind, java.lang.CharSequence, javax.lang.model.element.Element,
	 * javax.lang.model.element.AnnotationMirror)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e,
			AnnotationMirror a) {
		printMessage(kind, msg, e, a, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic
	 * .Kind, java.lang.CharSequence, javax.lang.model.element.Element,
	 * javax.lang.model.element.AnnotationMirror,
	 * javax.lang.model.element.AnnotationValue)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e,
			AnnotationMirror a, AnnotationValue v) {
		if (kind == Kind.ERROR) {
			_processingEnv.setErrorRaised(true);
		}
		AptProblem problem = BaseMessagerImpl.createProblem(kind, msg, e, a, v);
		this.diagnosticListener.report(new DiagnosticAdapter(kind, problem));
	}

}
