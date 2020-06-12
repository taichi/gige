package io.gige;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardJavaFileManager;

@SupportedAnnotationTypes("*")
public class AssertionProcessor extends AbstractProcessor {

  final StandardJavaFileManager manager;

  AssertionBlock assertions;

  Error error;

  AssertionProcessor(StandardJavaFileManager manager, AssertionBlock assertions) {
    this.manager = manager;
    this.assertions = assertions;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public boolean process(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      var context = new ProcessorContext(this.manager, this.processingEnv, roundEnv);
      try {
        this.assertions.apply(context);
      } catch (Exception e) {
        this.processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
      } catch (Error e) {
        this.error = e;
      }
    }
    return false;
  }

  public void rethrowOrNothing() {
    if (this.error != null) {
      throw this.error;
    }
  }
}
