package io.gige.compiler.internal;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.gige.CompilerContext;
import io.gige.Compilers;
import io.gige.Unit;
import io.gige.junit.CompilerRunner;

@RunWith(CompilerRunner.class)
public class HackElementsTest {

  @Compilers CompilerContext context;

  @Before
  public void setUp() throws Exception {
    this.context
        .set(Locale.JAPANESE)
        .setSourcePath("src/test/java", "src/test/resources")
        .set(diag -> System.out.println(diag));
  }

  @Test
  public void test() throws Exception {
    var annon =
        Unit.of(
            "z.MyAnon",
            "package z;\r\npublic @interface MyAnon { long value() default Long.MAX_VALUE;}");

    var cis = Unit.of("z.MyCls", "package z;\r\n" + "@MyAnon\r\n" + "public class MyCls {}");
    var pros = new MyProcessor();

    var result = this.context.set(annon, cis).set(pros).compile();
    Assert.assertTrue(result.success());
    var diags = result.getDiagnostics();
    Assert.assertTrue(diags.isEmpty());
  }
}
