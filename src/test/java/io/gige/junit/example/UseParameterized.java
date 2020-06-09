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
package io.gige.junit.example;

import static org.junit.Assert.assertTrue;
import io.gige.CompilationResult;
import io.gige.CompilerContext;
import io.gige.Compilers;
import io.gige.TestSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/** @author taichi */
@RunWith(Parameterized.class)
public class UseParameterized {

  @Parameters(name = "{0}")
  public static Compilers.Type[][] newContexts() {
    return new Compilers.Type[][] {{Compilers.Type.Standard}, {Compilers.Type.Eclipse}};
  }

  @Parameter public Compilers.Type type;

  CompilerContext context;

  @Before
  public void setUp() {
    this.context = new CompilerContext(this.type);
    this.context
        .setSourcePath("src/test/java", "src/test/resources")
        .set(diag -> System.out.println(diag))
        .setUnits(TestSource.class);
  }

  @After
  public void tearDown() throws Exception {
    // you must release external resources here.
    this.context.close();
  }

  @Test
  public void test() throws Exception {
    CompilationResult result = this.context.compile();
    assertTrue(result.success());
  }
}
