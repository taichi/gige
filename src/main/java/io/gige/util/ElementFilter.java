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

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * replacement of javax.lang.model.util.ElementFilter.<br>
 * this class built on Java8 Stream API.
 * 
 * @author taichi
 */
public interface ElementFilter {

	public static Stream<VariableElement> fieldsIn(TypeElement element) {
		return fieldsIn(element.getEnclosedElements());
	}

	public static <T extends Element> Stream<VariableElement> fieldsIn(
			Iterable<T> elements) {
		Objects.requireNonNull(elements);

		return fieldsIn(StreamSupport.stream(elements.spliterator(), false));
	}

	public static <T extends Element> Stream<VariableElement> fieldsIn(
			Stream<T> elements) {
		Objects.requireNonNull(elements);

		return elements.filter(fields()).map(VariableElement.class::cast);
	}

	public static <T extends Element> Predicate<T> fields() {
		return of(ElementKind.FIELD, ElementKind.ENUM_CONSTANT);
	}

	public static Stream<ExecutableElement> constructorsIn(TypeElement element) {
		return constructorsIn(element.getEnclosedElements());
	}

	public static <T extends Element> Stream<ExecutableElement> constructorsIn(
			Iterable<T> elements) {
		Objects.requireNonNull(elements);

		return constructorsIn(StreamSupport.stream(elements.spliterator(),
				false));
	}

	public static <T extends Element> Stream<ExecutableElement> constructorsIn(
			Stream<T> elements) {
		Objects.requireNonNull(elements);

		return elements.filter(constructors()).map(
				ExecutableElement.class::cast);
	}

	public static <T extends Element> Predicate<T> constructors() {
		return of(ElementKind.CONSTRUCTOR);
	}

	public static Stream<ExecutableElement> methodsIn(TypeElement element) {
		return methodsIn(element.getEnclosedElements());
	}

	public static <T extends Element> Stream<ExecutableElement> methodsIn(
			Iterable<T> elements) {
		Objects.requireNonNull(elements);

		return methodsIn(StreamSupport.stream(elements.spliterator(), false));
	}

	public static <T extends Element> Stream<ExecutableElement> methodsIn(
			Stream<T> elements) {
		Objects.requireNonNull(elements);

		return elements.filter(methods()).map(ExecutableElement.class::cast);
	}

	public static <T extends Element> Predicate<T> methods() {
		return of(ElementKind.METHOD);
	}

	public static <T extends Element> Stream<TypeElement> typesIn(
			Iterable<T> elements) {
		Objects.requireNonNull(elements);

		return typesIn(StreamSupport.stream(elements.spliterator(), false));
	}

	public static <T extends Element> Stream<TypeElement> typesIn(
			Stream<T> elements) {
		Objects.requireNonNull(elements);
		return elements.filter(types()).map(TypeElement.class::cast);
	}

	public static <T extends Element> Predicate<T> types() {
		return of(EnumSet.of(ElementKind.CLASS, ElementKind.ENUM,
				ElementKind.INTERFACE, ElementKind.ANNOTATION_TYPE));
	}

	public static <T extends Element> Stream<PackageElement> packagesIn(
			Iterable<T> elements) {
		Objects.requireNonNull(elements);

		return packagesIn(StreamSupport.stream(elements.spliterator(), false));
	}

	public static <T extends Element> Stream<PackageElement> packagesIn(
			Stream<T> elements) {
		Objects.requireNonNull(elements);

		return elements.filter(packages()).map(PackageElement.class::cast);
	}

	public static <T extends Element> Predicate<T> packages() {
		return of(ElementKind.PACKAGE);
	}

	public static <T extends Element> Predicate<T> of(ElementKind ek) {
		return of(EnumSet.of(ek));
	}

	public static <T extends Element> Predicate<T> of(ElementKind ek1,
			ElementKind ek2) {
		return of(EnumSet.of(ek1, ek2));
	}

	public static <T extends Element> Predicate<T> of(ElementKind ek1,
			ElementKind ek2, ElementKind ek3) {
		return of(EnumSet.of(ek1, ek2, ek3));
	}

	public static <T extends Element> Predicate<T> of(Set<ElementKind> condition) {
		Objects.requireNonNull(condition);

		return t -> condition.contains(t.getKind());
	}

	public static <T extends Element> Predicate<T> simpleName(CharSequence name) {
		Objects.requireNonNull(name);

		return t -> t.getSimpleName().contentEquals(name);
	}

	public static <T extends QualifiedNameable> Predicate<T> qualifiedName(
			CharSequence name) {
		Objects.requireNonNull(name);

		return t -> t.getQualifiedName().contentEquals(name);
	}
}
