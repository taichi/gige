# Gige

Gige is the Cross Compiler Annotation Processor testing Framework.

Gige supports...

* Open JDK compiler
* Oracle JDK compiler
* Eclipse compiler for Java

Gige also works with JUnit5 very well.

## Getting Started

### Add dependency to your build.gradle

```groovy
plugins {
  id 'java'
}

repositories.mavenCentral()

dependencies {
  testImplementation 'io.gige:gige:0.5.0'
  testImplementation 'org.junit.jupiter:junit-jupiter-api:5.6.0'
  testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.6.0'
}

java {
  sourceCompatibility = targetCompatibility = JavaVersion.VERSION_14
}

test {
  useJUnitPlatform()
}
```

### write test code

#### most simple code

```java
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import io.gige.*;
import io.gige.junit.CompilerExtension;

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

```

# License

Apache License, Version 2.0

# Lean from

* https://github.com/seasarorg/aptina
* https://github.com/google/compile-testing

# Badges
[![Circle CI](https://circleci.com/gh/taichi/gige/tree/develop.svg?style=svg)](https://circleci.com/gh/taichi/gige/tree/develop)


