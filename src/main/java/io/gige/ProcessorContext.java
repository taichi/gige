package io.gige;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.junit.jupiter.api.Assertions;

import io.gige.util.ElementFilter;
import io.gige.util.GigeTypes;
import io.gige.util.TypeHierarchy;
import io.gige.util.Zipper;

public class ProcessorContext {

  final StandardJavaFileManager manager;
  final ProcessingEnvironment processingEnvironment;
  final RoundEnvironment roundEnvironment;

  public ProcessorContext(
      StandardJavaFileManager manager,
      ProcessingEnvironment processingEnvironment,
      RoundEnvironment roundEnvironment) {
    super();
    this.manager = manager;
    this.processingEnvironment = processingEnvironment;
    this.roundEnvironment = roundEnvironment;
  }

  public StandardJavaFileManager getManager() {
    return this.manager;
  }

  public ProcessingEnvironment getProcessingEnvironment() {
    return this.processingEnvironment;
  }

  public RoundEnvironment getRoundEnvironment() {
    return this.roundEnvironment;
  }

  public Optional<TypeElement> getTypeElement(Class<?> clazz) {
    return this.getTypeElement(clazz.getCanonicalName());
  }

  public Optional<TypeElement> getTypeElement(String className) {
    try {
      return Optional.ofNullable(
          this.processingEnvironment.getElementUtils().getTypeElement(className));
    } catch (NullPointerException e) {
      return Optional.empty();
    }
  }

  public Optional<VariableElement> getField(TypeElement element, CharSequence name) {
    return ElementFilter.fieldsIn(element).filter(ElementFilter.simpleName(name)).findFirst();
  }

  protected Predicate<ExecutableElement> sizeFilter(int length) {
    return e -> e.getParameters().size() == length;
  }

  protected Predicate<ExecutableElement> argsFilter(String... argTypes) {
    return this.sizeFilter(argTypes.length).and(e -> this.isSameTypes(e, argTypes));
  }

  protected Predicate<ExecutableElement> argsFilter(Class<?>... argTypes) {
    return this.sizeFilter(argTypes.length).and(e -> this.isSameTypes(e, argTypes));
  }

  public Optional<ExecutableElement> getConstructor(TypeElement element) {
    return ElementFilter.constructorsIn(element).filter(this.sizeFilter(0)).findFirst();
  }

  public Optional<ExecutableElement> getConstructor(TypeElement element, Class<?>... argTypes) {
    return ElementFilter.constructorsIn(element).filter(this.argsFilter(argTypes)).findFirst();
  }

  public Optional<ExecutableElement> getConstructor(TypeElement element, String... argTypes) {
    return ElementFilter.constructorsIn(element).filter(this.argsFilter(argTypes)).findFirst();
  }

  public Optional<ExecutableElement> getMethod(TypeElement element, String name) {
    return ElementFilter.methodsIn(element)
        .filter(ElementFilter.simpleName(name))
        .filter(this.sizeFilter(0))
        .findFirst();
  }

  public Optional<ExecutableElement> getMethod(
      TypeElement element, String name, Class<?>... argTypes) {
    return ElementFilter.methodsIn(element)
        .filter(ElementFilter.simpleName(name))
        .filter(this.argsFilter(argTypes))
        .findFirst();
  }

  public Optional<ExecutableElement> getMethod(
      TypeElement element, String name, String... argTypes) {
    return ElementFilter.methodsIn(element)
        .filter(ElementFilter.simpleName(name))
        .filter(this.argsFilter(argTypes))
        .findFirst();
  }

  public Optional<ExecutableElement> findMethod(Class<?> clazz, String name, Class<?>... argTypes) {
    return this.getTypeElement(clazz)
        .flatMap(
            te ->
                TypeHierarchy.of(this.getProcessingEnvironment(), te)
                    .flatMap(ElementFilter::methodsIn)
                    .filter(ElementFilter.simpleName(name))
                    .filter(this.argsFilter(argTypes))
                    .findFirst());
  }

  public boolean isSameTypes(ExecutableElement signature, String[] right) {
    Stream<String> lNames = signature.getParameters().stream().map(ve -> ve.asType().toString());
    Stream<String> rNames = Stream.of(right).map(s -> s.replaceAll("\\h", ""));
    return Zipper.of(lNames, rNames, String::equals).allMatch(is -> is);
  }

  public boolean isSameTypes(ExecutableElement signature, Class<?>[] right) {
    Stream<TypeMirror> lTM = signature.getParameters().stream().map(Element::asType);
    Stream<TypeMirror> rTM =
        Stream.of(right)
            .map(this::getTypeElement)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Element::asType);
    return Zipper.of(lTM, rTM, this::isSameType).allMatch(is -> is);
  }

  public boolean isSameType(TypeMirror left, TypeMirror right) {
    return GigeTypes.isSameType(this.getProcessingEnvironment(), left, right);
  }

  public Optional<TypeMirror> getTypeMirror(Class<?> clazz) {
    return GigeTypes.getTypeMirror(this.getProcessingEnvironment(), clazz);
  }

  public Optional<TypeMirror> getTypeMirror(String className) {
    return GigeTypes.getTypeMirror(this.getProcessingEnvironment(), className);
  }

  public Optional<String> findOutputSource(Class<?> clazz) throws IOException {
    return this.findOutputSource(clazz.getCanonicalName());
  }

  public Optional<String> findOutputSource(String className) throws IOException {
    JavaFileObject obj =
        this.getManager()
            .getJavaFileForInput(StandardLocation.SOURCE_OUTPUT, className, Kind.SOURCE);
    return this.toString(obj);
  }

  public Optional<String> findOutputResource(String pkg, String filename) throws IOException {
    FileObject obj =
        this.getManager().getFileForInput(StandardLocation.SOURCE_OUTPUT, pkg, filename);
    return this.toString(obj);
  }

  public void assertEquals(Reader expected, String outputClassName) throws IOException {
    JavaFileObject obj =
        this.getManager()
            .getJavaFileForInput(StandardLocation.SOURCE_OUTPUT, outputClassName, Kind.SOURCE);
    Assertions.assertNotNull(obj);
    try (BufferedReader left = new BufferedReader(expected);
        BufferedReader right = new BufferedReader(obj.openReader(true))) {
      Asserts.assertEqualsByLine(left, right);
    }
  }

  public void assertEquals(String expectedSource, Class<?> outputClass) throws IOException {
    this.assertEquals(expectedSource, outputClass.getCanonicalName());
  }

  public void assertEquals(Path expectedSource, Class<?> outputClass) throws IOException {
    this.assertEquals(expectedSource, outputClass.getCanonicalName());
  }

  public void assertEquals(String expectedSource, String outputClassName) throws IOException {
    this.assertEquals(new StringReader(expectedSource), outputClassName);
  }

  public void assertEquals(Path expectedSource, String outputClassName) throws IOException {
    this.assertEquals(Files.newBufferedReader(expectedSource), outputClassName);
  }

  protected Optional<String> toString(FileObject obj) throws IOException {
    if (obj == null) {
      return Optional.empty();
    }
    CharSequence content = obj.getCharContent(true);
    if (content == null) {
      return Optional.empty();
    }
    return Optional.of(content.toString());
  }
}
