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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.gige.junit.CompilerRunner;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author taichi
 */
@RunWith(CompilerRunner.class)
public class CompilerContextTest {

	@Compilers
	CompilerContext context;

	@Before
	public void setUp() throws Exception {
		this.context.setCharset("UTF-8").set(Locale.JAPANESE)
				.setSourcePath("src/test/java", "src/test/resources")
				.set(diag -> System.out.println(diag))
				.setUnits(TestSource.class);
	}

	@Test
	public void success() throws Exception {
		TestProcessor processor = new TestProcessor();

		CompilationResult result = this.context.set(processor).compile();

		assertTrue(result.success());
		assertTrue(processor.called);

		assertFalse(result.getDiagnostics().isEmpty());
		assertNotNull(result.getEnvironment());
		assertNotNull(result.getManager());

		assertNotNull(result.getTypeElement(TestSource.class));
		assertNotNull(result.getTypeMirror(TestSource.class));

		Optional<String> src = result.findOutputSource("aaa.bbb.ccc.Ddd");
		assertTrue(src.isPresent());
		assertEquals("package aaa.bbb.ccc;public class Ddd {}", src.get());

		Optional<String> txt = result.findOutputResource("", "eee.txt");
		assertTrue(txt.isPresent());
		assertEquals("fff", txt.get());
	}

	@Test
	public void onTheFly() throws Exception {
		StringBuilder stb = new StringBuilder();
		stb.append("package test.on.the;");
		stb.append("@io.gige.TestAnnotation ");
		stb.append("public class Fly {}");

		this.context.set(Unit.of("test.on.the.Fly", stb.toString()));

		TestProcessor processor = new TestProcessor();

		CompilationResult result = this.context.set(processor).compile();

		assertTrue(result.success());
		assertTrue(processor.called);

		Optional<String> src = result.findOutputSource("aaa.bbb.ccc.Ddd");
		assertTrue(src.isPresent());
	}

	@Test
	public void diagnostics() throws Exception {
		DiagnosticProcessor processor = new DiagnosticProcessor();

		CompilationResult result = this.context.set(processor).compile();

		List<Diagnostic<? extends JavaFileObject>> msgs = result
				.getDiagnostics();
		assertEquals(3, msgs.size());

		assertEquals(Diagnostic.Kind.NOTE, msgs.get(0).getKind());
		assertEquals(Diagnostic.Kind.ERROR, msgs.get(1).getKind());
		assertEquals(Diagnostic.Kind.WARNING, msgs.get(2).getKind());

		Optional<Diagnostic<? extends JavaFileObject>> clz = msgs.stream()
				.filter(Diagnostics.filter(TestSource.class)).findFirst();
		assertTrue(clz.isPresent());

		Optional<Diagnostic<? extends JavaFileObject>> note = msgs.stream()
				.filter(Diagnostics.filter(Kind.NOTE)).findFirst();
		assertTrue(note.isPresent());

		Optional<Diagnostic<? extends JavaFileObject>> and = msgs
				.stream()
				.filter(Diagnostics.filter(TestSource.class).and(
						Diagnostics.filter(Kind.ERROR))).findFirst();
		assertTrue(and.isPresent());
	}

	@Test
	public void fields() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);

		Stream.of("aaa", "bbb", "ccc").forEach(
				name -> {
					VariableElement field = result.getField(element, name)
							.orElseThrow(AssertionError::new);
					assertEquals(name, field.getSimpleName().toString());
				});

		Optional<VariableElement> field = result.getField(element, "zzz");
		assertFalse(field.isPresent());
	}

	@Test
	public void defaultConstructor() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);

		Optional<ExecutableElement> defc = result.getConstructor(element);
		assertTrue(defc.isPresent());
	}

	@Test
	public void noConstructor() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);
		Optional<ExecutableElement> ctr = result.getConstructor(element,
				Object.class);
		assertFalse(ctr.isPresent());

		Optional<ExecutableElement> ctr2 = result.getConstructor(element,
				"java.lang.Object");
		assertFalse(ctr2.isPresent());
	}

	@Test
	public void intConstructor() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);
		Optional<ExecutableElement> ctr = result.getConstructor(element,
				int.class);
		assertTrue(ctr.isPresent());

		Optional<ExecutableElement> ctr2 = result
				.getConstructor(element, "int");
		assertTrue(ctr2.isPresent());
	}

	@Test
	public void arrayConstructor() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);
		Optional<ExecutableElement> ctr = result.getConstructor(element,
				String[].class);
		assertTrue(ctr.isPresent());

		Optional<ExecutableElement> ctr2 = result.getConstructor(element,
				"java.lang.String[]");
		assertTrue(ctr2.isPresent());
	}

	@Test
	public void genericConstructor() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);
		Optional<ExecutableElement> ctr = result.getConstructor(element,
				List.class);
		assertTrue(ctr.isPresent());

		Optional<ExecutableElement> ctr2 = result.getConstructor(element,
				"java.util.List<T>");
		assertTrue(ctr2.isPresent());
	}

	@Test
	public void noArgsMethod() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);

		Optional<ExecutableElement> mtd = result.getMethod(element, "aaa");
		assertTrue(mtd.isPresent());
	}

	@Test
	public void noMethod() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);
		Optional<ExecutableElement> ctr = result.getMethod(element, "aaa",
				Object.class);
		assertFalse(ctr.isPresent());

		Optional<ExecutableElement> ctr2 = result.getMethod(element, "aaa",
				"java.lang.Object");
		assertFalse(ctr2.isPresent());
	}

	@Test
	public void intMethod() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);

		Optional<ExecutableElement> mtd = result.getMethod(element, "aaa",
				int.class);
		assertTrue(mtd.isPresent());

		Optional<ExecutableElement> mtd2 = result.getMethod(element, "aaa",
				"int");
		assertTrue(mtd2.isPresent());
	}

	@Test
	public void arrayMethod() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);
		Optional<ExecutableElement> mtd = result.getMethod(element, "setBbb",
				String[].class);
		assertTrue(mtd.isPresent());

		Optional<ExecutableElement> mtd2 = result.getMethod(element, "setBbb",
				"java.lang.String[]");
		assertTrue(mtd2.isPresent());
	}

	@Test
	public void genericMethod() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);
		Optional<ExecutableElement> mtd = result.getMethod(element, "setCcc",
				List.class);
		assertTrue(mtd.isPresent());

		Optional<ExecutableElement> mtd2 = result.getMethod(element, "setCcc",
				"java.util.List<T>");
		assertTrue(mtd2.isPresent());
	}

	@Test
	public void genericMethodWithWhitespace() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		TypeElement element = result.getTypeElement(TestSource.class)
				.orElseThrow(AssertionError::new);

		Optional<ExecutableElement> mtd = result.getMethod(element, "of",
				"java.util.List< java.util.Map\t<java.lang.String, T>>");
		assertTrue(mtd.isPresent());
	}

	@Test
	public void typeMirror() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());

		assertEquals("boolean", result.getTypeMirror(boolean.class).get()
				.toString());
		assertEquals("int[]", result.getTypeMirror(int[].class).get()
				.toString());
		assertEquals("java.lang.String[][]",
				result.getTypeMirror(String[][].class).get().toString());

		assertEquals("java.lang.String[]",
				result.getTypeMirror("java.lang.String[]").get().toString());
		assertEquals("java.lang.String[][][]",
				result.getTypeMirror(String[][][].class).get().toString());
	}

	@Test
	public void resourceCopy() throws Exception {
		ResourceProcessor processor = new ResourceProcessor();
		CompilationResult result = this.context.set(processor).compile();
		assertTrue(result.success());
		assertTrue(processor.found);
	}
}
