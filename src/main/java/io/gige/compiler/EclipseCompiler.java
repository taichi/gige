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
package io.gige.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.eclipse.jdt.internal.compiler.apt.util.EclipseFileManager;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;

import io.gige.compiler.internal.ClasspathContainer;
import io.gige.compiler.internal.CompilationTaskImpl;

/** @author taichi */
public class EclipseCompiler implements JavaCompiler {

  StandardJavaFileManager provided;
  FileSystem filesystem;

  @Override
  public CompilationTask getTask(
      Writer out,
      JavaFileManager fileManager,
      DiagnosticListener<? super JavaFileObject> diagnosticListener,
      Iterable<String> options,
      Iterable<String> classes,
      Iterable<? extends JavaFileObject> compilationUnits) {

    for (Iterator<String> i = options.iterator(); i.hasNext(); ) {
      fileManager.handleOption(i.next(), i);
    }

    PrintWriter pw = out == null ? new PrintWriter(System.err, true) : new PrintWriter(out);
    CompilationTaskImpl task = new CompilationTaskImpl();
    task.configure(
        pw,
        fileManager,
        this.provided,
        this.filesystem,
        diagnosticListener,
        options,
        classes,
        compilationUnits);
    return task;
  }

  @Override
  public StandardJavaFileManager getStandardFileManager(
      DiagnosticListener<? super JavaFileObject> diagnosticListener,
      Locale locale,
      Charset charset) {
    if (this.provided == null) {
      this.provided = this.newFileManager(locale, charset);
      this.filesystem = ClasspathContainer.configure(this.provided);
    }
    return this.provided;
  }

  protected StandardJavaFileManager newFileManager(Locale locale, Charset charset) {
    final String OS = "os.name";
    String current = System.getProperty(OS);
    try {
      System.setProperty(OS, "xxx");
      return new EclipseFileManager(locale, charset) {
        @Override
        public void close() throws IOException {
          super.close();
          EclipseCompiler.this.filesystem.cleanup();
          EclipseCompiler.this.provided = null;
          EclipseCompiler.this.filesystem = null;
        }
      };
    } finally {
      System.setProperty(OS, current);
    }
  }

  @Override
  public int run(InputStream in, OutputStream out, OutputStream err, String... arguments) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<SourceVersion> getSourceVersions() {
    return EnumSet.allOf(SourceVersion.class);
  }

  @Override
  public int isSupportedOption(String option) {
    return 0;
  }

  @SuppressWarnings("unchecked")
  public static void forceInstall() throws Exception {
    Class<?> tp = ToolProvider.class;
    Method mtd = tp.getDeclaredMethod("instance");
    mtd.setAccessible(true);
    Field fld = tp.getDeclaredField("toolClasses");
    fld.setAccessible(true);
    Map<String, Reference<Class<?>>> map =
        (Map<String, Reference<Class<?>>>) fld.get(mtd.invoke(tp));
    SoftReference<Class<?>> ref = new SoftReference<Class<?>>(EclipseCompiler.class);
    map.put("com.sun.tools.javac.api.JavacTool", ref);
  }
}
