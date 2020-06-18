package io.gige.compiler.internal;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import io.gige.CompilerContext;
import io.gige.Compilers;
import io.gige.Unit;
import io.gige.junit.CompilerExtension;

@ExtendWith(CompilerExtension.class)
public class HackElementsTest {

  @TestTemplate
  @Compilers
  public void test(CompilerContext context) throws Exception {
    context
        .set(Locale.JAPANESE)
        .setSourcePath("src/test/java", "src/test/resources")
        .set(diag -> System.out.println(diag));
    var annon =
        Unit.of(
            "z.MyAnon",
            "package z;\r\npublic @interface MyAnon { long value() default Long.MAX_VALUE;}");

    var cis = Unit.of("z.MyCls", "package z;\r\n" + "@MyAnon\r\n" + "public class MyCls {}");
    var pros = new MyProcessor();

    var result = context.set(annon, cis).set(pros).compile();
    Assertions.assertTrue(result.success());
    var diags = result.getDiagnostics();
    Assertions.assertTrue(diags.isEmpty());
  }
}
