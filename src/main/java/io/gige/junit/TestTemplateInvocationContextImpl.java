/*
 * Copyright 2014 - 2020 SATO taichi
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
package io.gige.junit;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import io.gige.CompilerContext;
import io.gige.Compilers;

/** @author taichi */
class TestTemplateInvocationContextImpl implements TestTemplateInvocationContext {

  String name;

  CompilerContext ctx;

  TestTemplateInvocationContextImpl(String name, CompilerContext ctx) {
    this.name = name;
    this.ctx = ctx;
  }

  static TestTemplateInvocationContext of(Compilers.Type t) {
    return new TestTemplateInvocationContextImpl(t.name(), new CompilerContext(t));
  }

  @Override
  public String getDisplayName(int invocationIndex) {
    return this.name;
  }

  @Override
  public List<Extension> getAdditionalExtensions() {
    return Arrays.asList(
        new ParameterResolver() {
          @Override
          public boolean supportsParameter(
              ParameterContext parameterContext, ExtensionContext extensionContext) {
            return parameterContext.getParameter().getType().equals(CompilerContext.class);
          }

          @Override
          public Object resolveParameter(
              ParameterContext parameterContext, ExtensionContext extensionContext) {
            return TestTemplateInvocationContextImpl.this.ctx;
          }
        },
        new AfterTestExecutionCallback() {
          @Override
          public void afterTestExecution(ExtensionContext context) throws Exception {
            TestTemplateInvocationContextImpl.this.ctx.close();
          }
        });
  }
}
