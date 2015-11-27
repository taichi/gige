# Gige

Gige is the Cross Compiler testing Framework.

Gige supports...

* Oracle JDK compiler
* Eclipse compiler for Java

Gige also works with JUnit very well.

## Getting Started

### Add dependency to your build.gradle

```groovy
apply plugin: 'java'

repositories.jcenter()

dependencies {
    testCompile 'io.gige:gige:0.4.2'
}

sourceCompatibility = targetCompatibility = 1.8
```

### write test code

#### most simple code

```java
import static org.junit.Assert.assertTrue;
import io.gige.*;
import io.gige.junit.CompilerRunner;

import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(CompilerRunner.class)
public class UseCompilerRunner {

	@Compilers
	CompilerContext context;

	@Before
	public void setUp() {
		// CompilerRunner close CompilerContext automatically.
		this.context.setSourcePath("src/test/java", "src/test/resources")
				.set(diag -> System.out.println(diag))
				.setUnits(TestSource.class);
	}

	@Test
	public void test() throws Exception {
		CompilationResult result = this.context.compile();
		assertTrue(result.success());
	}
}
```
this is the simple way. but some magic.


### standard code

```java
import static org.junit.Assert.assertTrue;
import io.gige.*;

import org.junit.experimental.theories.*;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class UseTheories {

	@DataPoints
	public static Compilers.Type[] jdk = Compilers.Type.values();

	CompilerContext setUp(CompilerContext context) {
		return context.setSourcePath("src/test/java", "src/test/resources")
				.set(diag -> System.out.println(diag));
	}

	@Theory
	public void test(Compilers.Type type) throws Exception {
		// you must release external resources
		try (CompilerContext context = new CompilerContext(type)) {
			CompilationResult result = setUp(context)
					.setUnits(TestSource.class).compile();
			assertTrue(result.success());
		}
	}
}
```
this is the most standard way to write test code.
but `Theories` is experimental yet.


### not recommended way

```java
import static org.junit.Assert.assertTrue;
import io.gige.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class UseParameterized {

	@Parameters(name = "{0}")
	public static Compilers.Type[][] newContexts() {
		return new Compilers.Type[][] { { Compilers.Type.Standard },
				{ Compilers.Type.Eclipse } };
	}

	@Parameter
	public Compilers.Type type;

	CompilerContext context;

	@Before
	public void setUp() {
		this.context = new CompilerContext(this.type);
		this.context.setSourcePath("src/test/java", "src/test/resources")
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
```

this way isn't recommended way because this is very redundant code, but works well.



# License

Apache License, Version 2.0

# Lean from

* https://github.com/seasarorg/aptina
* https://github.com/google/compile-testing

# Badges
[![wercker status](https://app.wercker.com/status/b61caeec2c22ee5147590de508904961/m "wercker status")](https://app.wercker.com/project/bykey/b61caeec2c22ee5147590de508904961)

