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
package io.gige.compiler.internal;

import javax.annotation.processing.Processor;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseAnnotationProcessorManager;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.apt.dispatch.ProcessorInfo;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

/** @author taichi */
public class AnnotationProcessorManager extends BaseAnnotationProcessorManager {

  public AnnotationProcessorManager(BaseProcessingEnvImpl env) {
    this._processingEnv = env;
  }

  public void configure(Iterable<String> options) {
    for (String s : options) {
      if (s.equalsIgnoreCase("-XprintProcessorInfo")) {
        this._printProcessorInfo = true;
      }
      if (s.equalsIgnoreCase("-XprintRounds")) {
        this._printRounds = true;
      }
    }
  }

  @Override
  public void setProcessors(Object[] processors) {
    for (Object o : processors) {
      if (o instanceof Processor) {
        Processor p = (Processor) o;
        p.init(this._processingEnv);
        ProcessorInfo pi = new ProcessorInfo(p);
        this._processors.add(pi);
      }
    }
  }

  @Override
  public ProcessorInfo discoverNextProcessor() {
    return null; // do nothing.
  }

  @Override
  public void reportProcessorException(Processor p, Exception e) {
    throw new AbortCompilation(null, e);
  }
}
