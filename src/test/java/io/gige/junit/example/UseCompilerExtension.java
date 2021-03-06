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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import io.gige.CompilationResult;
import io.gige.CompilerContext;
import io.gige.Compilers;
import io.gige.TestSource;
import io.gige.junit.CompilerExtension;

/** @author taichi */
@ExtendWith(CompilerExtension.class)
public class UseCompilerExtension {

  @TestTemplate
  @Compilers
  public void test(CompilerContext context) throws Exception {
    // CompilerExtension close CompilerContext automatically.
    context
        .setSourcePath("src/test/java", "src/test/resources")
        .set(diag -> System.out.println(diag))
        .setUnits(TestSource.class);
    CompilationResult result = context.compile();
    Assertions.assertTrue(result.success());
  }
}
