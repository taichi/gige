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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.gige.junit.CompilerRunner;

/** @author taichi */
@RunWith(CompilerRunner.class)
public class CompilerContextTest {

  @Compilers CompilerContext context;

  @Before
  public void setUp() throws Exception {
    this.context
        .set(Locale.JAPANESE)
        .setSourcePath("src/test/java", "src/test/resources")
        .set(diag -> System.out.println(diag))
        .setUnits(TestSource.class);
  }

  @Test
  public void success() throws Exception {
    TestProcessor processor = new TestProcessor();

    CompilationResult result =
        this.context
            .set(processor)
            .compile(
                ctx -> {
                  Assert.assertTrue(processor.called);
                  Assert.assertNotNull(ctx.getTypeElement(TestSource.class));
                  Assert.assertNotNull(ctx.getTypeMirror(TestSource.class));

                  Optional<String> src = ctx.findOutputSource("aaa.bbb.ccc.Ddd");
                  Assert.assertTrue(src.isPresent());
                  Assert.assertEquals("package aaa.bbb.ccc;public class Ddd {}", src.get());

                  Optional<String> txt = ctx.findOutputResource("", "eee.txt");
                  Assert.assertTrue(txt.isPresent());
                  Assert.assertEquals("fff", txt.get());
                });

    Assert.assertTrue(result.success());
    Assert.assertFalse(result.getDiagnostics().isEmpty());
  }

  @Test
  public void onTheFly() throws Exception {
    StringBuilder stb = new StringBuilder();
    stb.append("package test.on.the;");
    stb.append("@io.gige.TestAnnotation ");
    stb.append("public class Fly {}");

    this.context.set(Unit.of("test.on.the.Fly", stb.toString()));

    TestProcessor processor = new TestProcessor();

    CompilationResult result =
        this.context
            .set(processor)
            .compile(
                ctx -> {
                  Optional<String> src = ctx.findOutputSource("aaa.bbb.ccc.Ddd");
                  Assert.assertTrue(src.isPresent());
                });

    Assert.assertTrue(result.success());
    Assert.assertTrue(processor.called);
  }

  @Test
  public void diagnostics() throws Exception {
    DiagnosticProcessor processor = new DiagnosticProcessor();

    CompilationResult result = this.context.set(processor).compile();

    List<Diagnostic<? extends JavaFileObject>> msgs = result.getDiagnostics();
    Assert.assertEquals(3, msgs.size());

    Assert.assertEquals(Diagnostic.Kind.NOTE, msgs.get(0).getKind());
    Assert.assertEquals(Diagnostic.Kind.ERROR, msgs.get(1).getKind());
    Assert.assertEquals(Diagnostic.Kind.WARNING, msgs.get(2).getKind());

    Optional<Diagnostic<? extends JavaFileObject>> clz =
        msgs.stream().filter(Diagnostics.filter(TestSource.class)).findFirst();
    Assert.assertTrue(clz.isPresent());

    Optional<Diagnostic<? extends JavaFileObject>> note =
        msgs.stream().filter(Diagnostics.filter(Kind.NOTE)).findFirst();
    Assert.assertTrue(note.isPresent());

    Optional<Diagnostic<? extends JavaFileObject>> and =
        msgs.stream()
            .filter(Diagnostics.filter(TestSource.class).and(Diagnostics.filter(Kind.ERROR)))
            .findFirst();
    Assert.assertTrue(and.isPresent());
  }

  @Test
  public void fields() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);

              Stream.of("aaa", "bbb", "ccc")
                  .forEach(
                      name -> {
                        VariableElement field =
                            ctx.getField(element, name).orElseThrow(AssertionError::new);
                        Assert.assertEquals(name, field.getSimpleName().toString());
                      });

              Optional<VariableElement> field = ctx.getField(element, "zzz");
              Assert.assertFalse(field.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void defaultConstructor() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);

              Optional<ExecutableElement> defc = ctx.getConstructor(element);
              Assert.assertTrue(defc.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void noConstructor() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> ctr = ctx.getConstructor(element, Object.class);
              Assert.assertFalse(ctr.isPresent());

              Optional<ExecutableElement> ctr2 = ctx.getConstructor(element, "java.lang.Object");
              Assert.assertFalse(ctr2.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void intConstructor() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> ctr = ctx.getConstructor(element, int.class);
              Assert.assertTrue(ctr.isPresent());

              Optional<ExecutableElement> ctr2 = ctx.getConstructor(element, "int");
              Assert.assertTrue(ctr2.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void arrayConstructor() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> ctr = ctx.getConstructor(element, String[].class);
              Assert.assertTrue(ctr.isPresent());

              Optional<ExecutableElement> ctr2 = ctx.getConstructor(element, "java.lang.String[]");
              Assert.assertTrue(ctr2.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void genericConstructor() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> ctr = ctx.getConstructor(element, List.class);
              Assert.assertTrue(ctr.isPresent());

              Optional<ExecutableElement> ctr2 = ctx.getConstructor(element, "java.util.List<T>");
              Assert.assertTrue(ctr2.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void noArgsMethod() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);

              Optional<ExecutableElement> mtd = ctx.getMethod(element, "aaa");
              Assert.assertTrue(mtd.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void noMethod() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> ctr = ctx.getMethod(element, "aaa", Object.class);
              Assert.assertFalse(ctr.isPresent());

              Optional<ExecutableElement> ctr2 = ctx.getMethod(element, "aaa", "java.lang.Object");
              Assert.assertFalse(ctr2.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void intMethod() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);

              Optional<ExecutableElement> mtd = ctx.getMethod(element, "aaa", int.class);
              Assert.assertTrue(mtd.isPresent());

              Optional<ExecutableElement> mtd2 = ctx.getMethod(element, "aaa", "int");
              Assert.assertTrue(mtd2.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void arrayMethod() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> mtd = ctx.getMethod(element, "setBbb", String[].class);
              Assert.assertTrue(mtd.isPresent());

              Optional<ExecutableElement> mtd2 =
                  ctx.getMethod(element, "setBbb", "java.lang.String[]");
              Assert.assertTrue(mtd2.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void genericMethod() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> mtd = ctx.getMethod(element, "setCcc", List.class);
              Assert.assertTrue(mtd.isPresent());

              Optional<ExecutableElement> mtd2 =
                  ctx.getMethod(element, "setCcc", "java.util.List<T>");
              Assert.assertTrue(mtd2.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void genericMethodWithWhitespace() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);

              Optional<ExecutableElement> mtd =
                  ctx.getMethod(
                      element, "of", "java.util.List< java.util.Map\t<java.lang.String, T>>");
              Assert.assertTrue(mtd.isPresent());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void typeMirror() throws Exception {
    CompilationResult result =
        this.context.compile(
            ctx -> {
              Assert.assertEquals("boolean", ctx.getTypeMirror(boolean.class).get().toString());
              Assert.assertEquals("int[]", ctx.getTypeMirror(int[].class).get().toString());
              Assert.assertEquals(
                  "java.lang.String[][]", ctx.getTypeMirror(String[][].class).get().toString());

              Assert.assertEquals(
                  "java.lang.String[]", ctx.getTypeMirror("java.lang.String[]").get().toString());
              Assert.assertEquals(
                  "java.lang.String[][][]", ctx.getTypeMirror(String[][][].class).get().toString());
            });
    Assert.assertTrue(result.success());
  }

  @Test
  public void resourceCopy() throws Exception {
    ResourceProcessor processor = new ResourceProcessor();
    CompilationResult result = this.context.set(processor).compile();
    Assert.assertTrue(result.success());
    Assert.assertTrue(processor.found);
  }
}
