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

import static org.junit.Assert.assertNotNull;
import io.gige.util.ElementFilter;
import io.gige.util.GigeTypes;
import io.gige.util.TypeHierarchy;
import io.gige.util.Zipper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

/**
 * @author taichi
 */
public class CompilationResult implements AutoCloseable {

	final boolean success;
	final StandardJavaFileManager manager;
	final List<Diagnostic<? extends JavaFileObject>> storage;
	final ProcessingEnvironment env;

	public CompilationResult(boolean success, StandardJavaFileManager manager,
			List<Diagnostic<? extends JavaFileObject>> storage,
			ProcessingEnvironment env) {
		this.success = success;
		this.manager = manager;
		this.storage = storage;
		this.env = env;
	}

	public boolean success() {
		return this.success;
	};

	public StandardJavaFileManager getManager() {
		return this.manager;
	}

	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
		return this.storage;
	}

	public ProcessingEnvironment getEnvironment() {
		return this.env;
	}

	public Optional<TypeElement> getTypeElement(Class<?> clazz) {
		return getTypeElement(clazz.getCanonicalName());
	}

	public Optional<TypeElement> getTypeElement(String className) {
		try {
			return Optional.ofNullable(this.env.getElementUtils()
					.getTypeElement(className));
		} catch (NullPointerException e) {
			return Optional.empty();
		}
	}

	public Optional<VariableElement> getField(TypeElement element,
			CharSequence name) {
		return ElementFilter.fieldsIn(element)
				.filter(ElementFilter.simpleName(name)).findFirst();
	}

	protected Predicate<ExecutableElement> sizeFilter(int length) {
		return e -> e.getParameters().size() == length;
	}

	protected Predicate<ExecutableElement> argsFilter(String... argTypes) {
		return sizeFilter(argTypes.length).and(e -> isSameTypes(e, argTypes));
	}

	protected Predicate<ExecutableElement> argsFilter(Class<?>... argTypes) {
		return sizeFilter(argTypes.length).and(e -> isSameTypes(e, argTypes));
	}

	public Optional<ExecutableElement> getConstructor(TypeElement element) {
		return ElementFilter.constructorsIn(element).filter(sizeFilter(0))
				.findFirst();
	}

	public Optional<ExecutableElement> getConstructor(TypeElement element,
			Class<?>... argTypes) {
		return ElementFilter.constructorsIn(element)
				.filter(argsFilter(argTypes)).findFirst();
	}

	public Optional<ExecutableElement> getConstructor(TypeElement element,
			String... argTypes) {
		return ElementFilter.constructorsIn(element)
				.filter(argsFilter(argTypes)).findFirst();
	}

	public Optional<ExecutableElement> getMethod(TypeElement element,
			String name) {
		return ElementFilter.methodsIn(element)
				.filter(ElementFilter.simpleName(name)).filter(sizeFilter(0))
				.findFirst();
	}

	public Optional<ExecutableElement> getMethod(TypeElement element,
			String name, Class<?>... argTypes) {
		return ElementFilter.methodsIn(element)
				.filter(ElementFilter.simpleName(name))
				.filter(argsFilter(argTypes)).findFirst();
	}

	public Optional<ExecutableElement> getMethod(TypeElement element,
			String name, String... argTypes) {
		return ElementFilter.methodsIn(element)
				.filter(ElementFilter.simpleName(name))
				.filter(argsFilter(argTypes)).findFirst();
	}

	/**
	 * find method from type hierarchy
	 */
	public Optional<ExecutableElement> findMethod(Class<?> clazz, String name,
			Class<?>... argTypes) {
		return getTypeElement(clazz).flatMap(
				te -> TypeHierarchy.of(getEnvironment(), te)
						.flatMap(ElementFilter::methodsIn)
						.filter(ElementFilter.simpleName(name))
						.filter(argsFilter(argTypes)).findFirst());
	}

	public boolean isSameTypes(ExecutableElement signature, String[] right) {
		Stream<String> lNames = signature.getParameters().stream()
				.map(ve -> ve.asType().toString());
		Stream<String> rNames = Stream.of(right).map(
				s -> s.replaceAll("\\h", ""));
		return Zipper.of(lNames, rNames, String::equals).allMatch(is -> is);
	}

	public boolean isSameTypes(ExecutableElement signature, Class<?>[] right) {
		Stream<TypeMirror> lTM = signature.getParameters().stream()
				.map(Element::asType);
		Stream<TypeMirror> rTM = Stream.of(right).map(this::getTypeElement)
				.filter(Optional::isPresent).map(Optional::get)
				.map(Element::asType);
		return Zipper.of(lTM, rTM, this::isSameType).allMatch(is -> is);
	}

	public boolean isSameType(TypeMirror left, TypeMirror right) {
		return GigeTypes.isSameType(getEnvironment(), left, right);
	}

	public Optional<TypeMirror> getTypeMirror(Class<?> clazz) {
		return GigeTypes.getTypeMirror(getEnvironment(), clazz);
	}

	public Optional<TypeMirror> getTypeMirror(String className) {
		return GigeTypes.getTypeMirror(getEnvironment(), className);
	}

	public Optional<String> findOutputSource(Class<?> clazz) throws IOException {
		return findOutputSource(clazz.getCanonicalName());
	}

	public Optional<String> findOutputSource(String className)
			throws IOException {
		JavaFileObject obj = getManager().getJavaFileForInput(
				StandardLocation.SOURCE_OUTPUT, className, Kind.SOURCE);
		return toString(obj);
	}

	public Optional<String> findOutputResource(String pkg, String filename)
			throws IOException {
		FileObject obj = getManager().getFileForInput(
				StandardLocation.SOURCE_OUTPUT, pkg, filename);
		return toString(obj);
	}

	public void assertEquals(Reader expected, String outputClassName)
			throws IOException {
		JavaFileObject obj = getManager().getJavaFileForInput(
				StandardLocation.SOURCE_OUTPUT, outputClassName, Kind.SOURCE);
		assertNotNull(obj);
		try (BufferedReader left = new BufferedReader(expected);
				BufferedReader right = new BufferedReader(obj.openReader(true))) {
			Asserts.assertEqualsByLine(left, right);
		}
	}

	public void assertEquals(String expectedSource, Class<?> outputClass)
			throws IOException {
		assertEquals(expectedSource, outputClass.getCanonicalName());
	}

	public void assertEquals(Path expectedSource, Class<?> outputClass)
			throws IOException {
		assertEquals(expectedSource, outputClass.getCanonicalName());
	}

	public void assertEquals(String expectedSource, String outputClassName)
			throws IOException {
		assertEquals(new StringReader(expectedSource), outputClassName);
	}

	public void assertEquals(Path expectedSource, String outputClassName)
			throws IOException {
		assertEquals(Files.newBufferedReader(expectedSource), outputClassName);
	}

	protected Optional<String> toString(FileObject obj) throws IOException {
		if (obj == null) {
			return Optional.empty();
		}
		CharSequence content = obj.getCharContent(true);
		if (content == null) {
			return Optional.empty();
		}
		return Optional.of(content.toString());
	}

	@Override
	public void close() throws Exception {
		this.manager.close();
	}
}
