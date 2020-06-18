package io.gige;

@FunctionalInterface
public interface AssertionBlock {

  void apply(ProcessorContext context) throws Exception;
}
