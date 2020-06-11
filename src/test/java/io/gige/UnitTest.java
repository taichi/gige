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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.gige.junit.CompilerRunner;

/** @author taichi */
@RunWith(CompilerRunner.class)
public class UnitTest {

  @Compilers CompilerContext context;

  @Before
  public void setUp() throws Exception {
    this.context.setSourcePath("src/test/java").set(System.out::println);
  }

  @Test
  public void test() throws Exception {
    this.context.set(Unit.of("aaa.Bbb", "package aaa;\npublic class Bbb {}"));
    CompilationResult result = this.context.compile();
    Assert.assertTrue(result.success());

    FileObject fo =
        result.env.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "aaa", "Bbb.java");
    Assert.assertNotNull(fo);

    CharSequence content = fo.getCharContent(true);
    Assert.assertNotNull(content);
    Assert.assertTrue(0 < content.length());

    Elements elems = result.env.getElementUtils();
    TypeElement te = elems.getTypeElement("aaa.Bbb");
    Assert.assertNotNull(te);
  }
}
