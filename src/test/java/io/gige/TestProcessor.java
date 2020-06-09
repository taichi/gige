package io.gige;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("io.gige.TestAnnotation")
public class TestProcessor extends AbstractProcessor {

  boolean called;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    if (called) {
      return false;
    }
    this.called = true;

    processingEnv.getMessager().printMessage(Kind.OTHER, "processing now!");

    Filer filer = this.processingEnv.getFiler();
    try {
      TypeElement te = annotations.iterator().next();
      JavaFileObject obj = filer.createSourceFile("aaa.bbb.ccc.Ddd", te);
      try (PrintWriter w = new PrintWriter(obj.openWriter())) {
        w.print("package aaa.bbb.ccc;");
        w.print("public class Ddd {}");
      }
      FileObject file = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "eee.txt", te);
      try (PrintWriter w = new PrintWriter(file.openWriter())) {
        w.print("fff");
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return false;
  }
}
