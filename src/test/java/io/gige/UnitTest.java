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

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import io.gige.junit.CompilerExtension;

/** @author taichi */
@ExtendWith(CompilerExtension.class)
public class UnitTest {

  @TestTemplate
  @Compilers
  public void test(CompilerContext context) throws Exception {
    context.setSourcePath("src/test/java").set(System.out::println);
    context.set(Unit.of("aaa.Bbb", "package aaa;\npublic class Bbb {}"));
    CompilationResult result =
        context.compile(
            (ctx -> {
              FileObject fo =
                  ctx.getProcessingEnvironment()
                      .getFiler()
                      .getResource(StandardLocation.SOURCE_OUTPUT, "aaa", "Bbb.java");
              Assertions.assertNotNull(fo);

              CharSequence content = fo.getCharContent(true);
              Assertions.assertNotNull(content);
              Assertions.assertTrue(0 < content.length());

              Elements elems = ctx.getProcessingEnvironment().getElementUtils();
              TypeElement te = elems.getTypeElement("aaa.Bbb");
              Assertions.assertNotNull(te);
            }));
    Assertions.assertTrue(result.success());
  }
}
