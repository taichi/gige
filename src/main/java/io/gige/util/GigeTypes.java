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
package io.gige.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor8;

/**
 * @author taichi
 */
public class GigeTypes {

	static final Map<String, TypeKind> primitives;

	static {
		final Map<String, TypeKind> map = new HashMap<>();
		map.put(void.class.getName(), TypeKind.VOID);
		map.put(boolean.class.getName(), TypeKind.BOOLEAN);
		map.put(char.class.getName(), TypeKind.CHAR);
		map.put(byte.class.getName(), TypeKind.BYTE);
		map.put(short.class.getName(), TypeKind.SHORT);
		map.put(int.class.getName(), TypeKind.INT);
		map.put(long.class.getName(), TypeKind.LONG);
		map.put(float.class.getName(), TypeKind.FLOAT);
		map.put(double.class.getName(), TypeKind.DOUBLE);
		primitives = Collections.unmodifiableMap(map);
	}

	public static TypeElement to(ProcessingEnvironment env,
			TypeMirror typeMirror) {
		Objects.requireNonNull(env);
		Objects.requireNonNull(typeMirror);
		Element element = env.getTypeUtils().asElement(typeMirror);
		if (element == null) {
			return null;
		}
		return element.accept(new SimpleElementVisitor8<TypeElement, Void>() {
			@Override
			public TypeElement visitType(TypeElement e, Void p) {
				return e;
			}
		}, null);
	}

	public static Optional<TypeMirror> getTypeMirror(ProcessingEnvironment env,
			Class<?> clazz) {
		return getTypeMirror(env, clazz.getCanonicalName());
	}

	public static Optional<TypeMirror> getTypeMirror(ProcessingEnvironment env,
			String className) {
		TypeElement te = env.getElementUtils().getTypeElement(className);
		if (te != null) {
			return Optional.of(te.asType());
		}
		if (className.endsWith("[]")) {
			final String newName = className.substring(0,
					className.length() - 2);
			return toArrayType(env, getTypeMirror(env, newName));
		}
		if (primitives.containsKey(className)) {
			return Optional.of(env.getTypeUtils().getPrimitiveType(
					primitives.get(className)));
		}
		return Optional.empty();
	}

	public static Optional<TypeMirror> toArrayType(ProcessingEnvironment env,
			Optional<TypeMirror> componentType) {
		return componentType.map(tm -> env.getTypeUtils().getArrayType(tm));
	}

	public static boolean isSameType(ProcessingEnvironment env, TypeMirror left,
			TypeMirror right) {
		Objects.requireNonNull(left);
		Objects.requireNonNull(right);
		Objects.requireNonNull(env);

		if (left.getKind() == TypeKind.NONE
				|| right.getKind() == TypeKind.NONE) {
			return false;
		}
		if (left.getKind() == TypeKind.NULL) {
			return right.getKind() == TypeKind.NULL;
		}
		if (right.getKind() == TypeKind.NULL) {
			return left.getKind() == TypeKind.NULL;
		}
		if (left.getKind() == TypeKind.VOID) {
			return right.getKind() == TypeKind.VOID;
		}
		if (right.getKind() == TypeKind.VOID) {
			return left.getKind() == TypeKind.VOID;
		}
		TypeMirror erasuredType1 = env.getTypeUtils().erasure(left);
		TypeMirror erasuredType2 = env.getTypeUtils().erasure(right);
		return env.getTypeUtils().isSameType(erasuredType1, erasuredType2)
				|| erasuredType1.equals(erasuredType2);
	}
}
