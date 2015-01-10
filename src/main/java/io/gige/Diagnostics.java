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

import java.util.function.Predicate;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * @author taichi
 */
public interface Diagnostics {

	static Predicate<Diagnostic<? extends JavaFileObject>> filter(Class<?> clazz) {
		return filter(clazz.getCanonicalName());
	}

	static Predicate<Diagnostic<? extends JavaFileObject>> filter(
			String className) {
		String path = className.replace('.', '/');
		return new Predicate<Diagnostic<? extends JavaFileObject>>() {
			@Override
			public boolean test(Diagnostic<? extends JavaFileObject> diag) {
				JavaFileObject obj = diag.getSource();
				return obj != null && obj.toUri().getPath().contains(path);
			}
		};
	}

	static Predicate<Diagnostic<? extends JavaFileObject>> filter(
			Diagnostic.Kind kind) {
		return new Predicate<Diagnostic<? extends JavaFileObject>>() {
			@Override
			public boolean test(Diagnostic<? extends JavaFileObject> t) {
				return t.getKind().equals(kind);
			}
		};
	}
}
