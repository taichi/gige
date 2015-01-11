/*
 * Copyright 2015 SATO taichi
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

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * @author taichi
 */
@SupportedAnnotationTypes("io.gige.TestAnnotation")
public class ResourceProcessor extends AbstractProcessor {

	boolean found;

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment env) {
		if (env.processingOver()) {
			return false;
		}

		Filer filer = this.processingEnv.getFiler();

		try {
			FileObject fo = filer.getResource(StandardLocation.CLASS_OUTPUT,
					"aaa", // jdk compiler needs legal package name.
					"META-INF/foo.txt");
			this.found = fo != null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

}
