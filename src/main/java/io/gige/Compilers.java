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

import static java.lang.annotation.ElementType.FIELD;
import io.gige.compiler.EclipseCompiler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * @author taichi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface Compilers {

	Type[] value() default { Type.Standard, Type.Eclipse };

	public enum Type implements Supplier<JavaCompiler> {
		Standard {
			@Override
			public JavaCompiler get() {
				return ToolProvider.getSystemJavaCompiler();
			}
		},
		Eclipse {
			@Override
			public JavaCompiler get() {
				return new EclipseCompiler();
			}
		};
	}
}
