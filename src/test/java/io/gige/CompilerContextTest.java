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

import java.util.Arrays;
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import io.gige.junit.CompilerExtension;

/** @author taichi */
@ExtendWith(CompilerExtension.class)
public class CompilerContextTest {

  @BeforeEach
  public void setUp(CompilerContext context) throws Exception {
    context
        .set(Locale.JAPANESE)
        .setSourcePath("src/test/java", "src/test/resources")
        .set(diag -> System.out.println(diag))
        .setUnits(TestSource.class);
  }

  @TestTemplate
  @Compilers
  public void success(CompilerContext context) throws Exception {
    TestProcessor processor = new TestProcessor();

    CompilationResult result =
        context
            .set(processor)
            .compile(
                ctx -> {
                  Assertions.assertTrue(processor.called);
                  Assertions.assertNotNull(ctx.getTypeElement(TestSource.class));
                  Assertions.assertNotNull(ctx.getTypeMirror(TestSource.class));

                  Optional<String> src = ctx.findOutputSource("aaa.bbb.ccc.Ddd");
                  Assertions.assertTrue(src.isPresent());
                  Assertions.assertEquals("package aaa.bbb.ccc;public class Ddd {}", src.get());

                  Optional<String> txt = ctx.findOutputResource("", "eee.txt");
                  Assertions.assertTrue(txt.isPresent());
                  Assertions.assertEquals("fff", txt.get());
                });

    Assertions.assertTrue(result.success());
    Assertions.assertFalse(result.getDiagnostics().isEmpty());
  }

  @TestTemplate
  public void onTheFly(CompilerContext context) throws Exception {
    StringBuilder stb = new StringBuilder();
    stb.append("package test.on.the;");
    stb.append("@io.gige.TestAnnotation ");
    stb.append("public class Fly {}");

    context.set(Unit.of("test.on.the.Fly", stb.toString()));

    TestProcessor processor = new TestProcessor();

    CompilationResult result =
        context
            .set(processor)
            .compile(
                ctx -> {
                  Optional<String> src = ctx.findOutputSource("aaa.bbb.ccc.Ddd");
                  Assertions.assertTrue(src.isPresent());
                });

    Assertions.assertTrue(result.success());
    Assertions.assertTrue(processor.called);
  }

  @TestTemplate
  public void diagnostics(CompilerContext context) throws Exception {
    DiagnosticProcessor processor = new DiagnosticProcessor();

    CompilationResult result = context.set(processor).compile();

    List<Diagnostic<? extends JavaFileObject>> msgs = result.getDiagnostics();
    Assertions.assertEquals(3, msgs.size());

    Assertions.assertEquals(Diagnostic.Kind.NOTE, msgs.get(0).getKind());
    Assertions.assertEquals(Diagnostic.Kind.ERROR, msgs.get(1).getKind());
    Assertions.assertEquals(Diagnostic.Kind.WARNING, msgs.get(2).getKind());

    Optional<Diagnostic<? extends JavaFileObject>> clz =
        msgs.stream().filter(Diagnostics.filter(TestSource.class)).findFirst();
    Assertions.assertTrue(clz.isPresent());

    Optional<Diagnostic<? extends JavaFileObject>> note =
        msgs.stream().filter(Diagnostics.filter(Kind.NOTE)).findFirst();
    Assertions.assertTrue(note.isPresent());

    Optional<Diagnostic<? extends JavaFileObject>> and =
        msgs.stream()
            .filter(Diagnostics.filter(TestSource.class).and(Diagnostics.filter(Kind.ERROR)))
            .findFirst();
    Assertions.assertTrue(and.isPresent());
  }

  @TestTemplate
  public void fields(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);

              Stream.of("aaa", "bbb", "ccc")
                  .forEach(
                      name -> {
                        VariableElement field =
                            ctx.getField(element, name).orElseThrow(AssertionError::new);
                        Assertions.assertEquals(name, field.getSimpleName().toString());
                      });

              Optional<VariableElement> field = ctx.getField(element, "zzz");
              Assertions.assertFalse(field.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void defaultConstructor(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);

              Optional<ExecutableElement> defc = ctx.getConstructor(element);
              Assertions.assertTrue(defc.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void noConstructor(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> ctr = ctx.getConstructor(element, Object.class);
              Assertions.assertFalse(ctr.isPresent());

              Optional<ExecutableElement> ctr2 = ctx.getConstructor(element, "java.lang.Object");
              Assertions.assertFalse(ctr2.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void intConstructor(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> ctr = ctx.getConstructor(element, int.class);
              Assertions.assertTrue(ctr.isPresent());

              Optional<ExecutableElement> ctr2 = ctx.getConstructor(element, "int");
              Assertions.assertTrue(ctr2.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void arrayConstructor(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> ctr = ctx.getConstructor(element, String[].class);
              Assertions.assertTrue(ctr.isPresent());

              Optional<ExecutableElement> ctr2 = ctx.getConstructor(element, "java.lang.String[]");
              Assertions.assertTrue(ctr2.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void genericConstructor(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> ctr = ctx.getConstructor(element, List.class);
              Assertions.assertTrue(ctr.isPresent());

              Optional<ExecutableElement> ctr2 = ctx.getConstructor(element, "java.util.List<T>");
              Assertions.assertTrue(ctr2.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void noArgsMethod(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);

              Optional<ExecutableElement> mtd = ctx.getMethod(element, "aaa");
              Assertions.assertTrue(mtd.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void noMethod(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> ctr = ctx.getMethod(element, "aaa", Object.class);
              Assertions.assertFalse(ctr.isPresent());

              Optional<ExecutableElement> ctr2 = ctx.getMethod(element, "aaa", "java.lang.Object");
              Assertions.assertFalse(ctr2.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void intMethod(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);

              Optional<ExecutableElement> mtd = ctx.getMethod(element, "aaa", int.class);
              Assertions.assertTrue(mtd.isPresent());

              Optional<ExecutableElement> mtd2 = ctx.getMethod(element, "aaa", "int");
              Assertions.assertTrue(mtd2.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void arrayMethod(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> mtd = ctx.getMethod(element, "setBbb", String[].class);
              Assertions.assertTrue(mtd.isPresent());

              Optional<ExecutableElement> mtd2 =
                  ctx.getMethod(element, "setBbb", "java.lang.String[]");
              Assertions.assertTrue(mtd2.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void genericMethod(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);
              Optional<ExecutableElement> mtd = ctx.getMethod(element, "setCcc", List.class);
              Assertions.assertTrue(mtd.isPresent());

              Optional<ExecutableElement> mtd2 =
                  ctx.getMethod(element, "setCcc", "java.util.List<T>");
              Assertions.assertTrue(mtd2.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void genericMethodWithWhitespace(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              TypeElement element =
                  ctx.getTypeElement(TestSource.class).orElseThrow(AssertionError::new);

              Optional<ExecutableElement> mtd =
                  ctx.getMethod(
                      element, "of", "java.util.List< java.util.Map\t<java.lang.String, T>>");
              Assertions.assertTrue(mtd.isPresent());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void typeMirror(CompilerContext context) throws Exception {
    CompilationResult result =
        context.compile(
            ctx -> {
              Assertions.assertEquals("boolean", ctx.getTypeMirror(boolean.class).get().toString());
              Assertions.assertEquals("int[]", ctx.getTypeMirror(int[].class).get().toString());
              Assertions.assertEquals(
                  "java.lang.String[][]", ctx.getTypeMirror(String[][].class).get().toString());

              Assertions.assertEquals(
                  "java.lang.String[]", ctx.getTypeMirror("java.lang.String[]").get().toString());
              Assertions.assertEquals(
                  "java.lang.String[][][]", ctx.getTypeMirror(String[][][].class).get().toString());
            });
    Assertions.assertTrue(result.success());
  }

  @TestTemplate
  public void resourceCopy(CompilerContext context) throws Exception {
    ResourceProcessor processor = new ResourceProcessor();
    CompilationResult result = context.set(processor).compile();
    Assertions.assertTrue(result.success());
    Assertions.assertTrue(processor.found);
  }

  @TestTemplate
  public void sourceOption(CompilerContext context) throws Exception {
    var result = context.setOptions(Arrays.asList("--enable-preview", "-source", "14")).compile();
    Assertions.assertTrue(result.success());
  }
}
