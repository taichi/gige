package io.gige.compiler.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class MyProcessor extends AbstractProcessor {
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return new HashSet<>(Arrays.asList("*"));
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      return false;
    }
    Elements elms = this.processingEnv.getElementUtils();
    Set<? extends Element> set = roundEnv.getRootElements();
    for (Element element : set) {
      for (AnnotationMirror am : element.getAnnotationMirrors()) {
        System.out.println(elms.getElementValuesWithDefaults(am));
      }
    }
    return false;
  }
}
