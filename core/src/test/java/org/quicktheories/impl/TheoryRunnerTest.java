package org.quicktheories.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.quicktheories.generators.SourceDSL.arbitrary;

import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quicktheories.core.Configuration;
import org.quicktheories.core.Gen;
import org.quicktheories.core.Guidance;
import org.quicktheories.core.NoGuidance;
import org.quicktheories.core.PseudoRandom;
import org.quicktheories.core.Reporter;
import org.quicktheories.core.Strategy;

@RunWith(MockitoJUnitRunner.class)
public class TheoryRunnerTest {

  Function<PseudoRandom, Guidance> guidance = prng -> new NoGuidance();
  TheoryRunner<Integer, Integer> testee;

  Strategy strategy;

  @Mock
  Reporter reporter;

  @Before
  public void setup() {
    strategy = new Strategy(Configuration.defaultPRNG(0), 100, 0, 100, 10, reporter, guidance);
  }

  @Test
  public void shouldNotFalisifyTheTruth() {
    testee = makeTesteeFor(arbitrary().pick(1, 2, 3, 4, 5));
    testee.check(i -> true);
    verify(reporter, never()).falisification(anyLong(), anyInt(),
        any(Object.class), anySmallerValues(), any());
  }

  @Test
  public void shouldFalisifyUniversalFalsehood() {
    testee = makeTesteeFor(arbitrary().pick(1, 2, 3, 4, 5));
    testee.check(i -> false);
    verify(reporter, times(1)).falisification(anyLong(), anyInt(),
        any(Object.class), anySmallerValues(), any());
  }

  @Test
  public void shouldFalsifyPartialTruth() {
    testee = makeTesteeFor(arbitrary().pick(1, 2, 3, 4, 5));
    testee.check(i -> i > 3);
    verify(reporter, times(1)).falisification(anyLong(), anyInt(),
        any(Object.class), anySmallerValues(), any());
  }

  @Test
  public void shouldFalsifyWhenPredicateThrowsException() {
    testee = makeTesteeFor(arbitrary().pick(1, 2, 3, 4, 5));
    testee.check(i -> {
      if (i > 3) {
        throw new AssertionError();
      }
      return true;
    });
    verify(reporter, times(1)).falisification(anyLong(), anyInt(),
        any(Object.class), any(Throwable.class), anySmallerValues(),
        any());
  }

  @Test
  public void shouldReportSmallestFalsifyingValueFound() {
    testee = makeTesteeFor(arbitrary().pick(1, 2, 3, 4, 5));
    testee.check(i -> i <= 3);
    verify(reporter, times(1)).falisification(anyLong(), anyInt(), eq(4),
        anySmallerValues(), any());
  }

  @Test
  public void shouldConstrainGeneratorsByAssumptions() {
    testee = makeTesteeFor(arbitrary().pick(1, 2, 3, 4, 5).assuming(
        i -> i != 4));
    testee.check(i -> i <= 3);
    verify(reporter, times(1)).falisification(anyLong(), anyInt(), eq(5),
        anySmallerValues(), any());
  }

  @Test
  public void shouldReportInitialSeed() {
    long seed = 42;
    strategy = new Strategy(Configuration.defaultPRNG(seed), 10, 0, 10, 10, reporter, guidance);
    testee = makeTesteeFor(arbitrary().pick(1));
    testee.check(i -> false);
    verify(reporter, times(1)).falisification(eq(seed), anyInt(), anyInt(),
        anySmallerValues(), any());
  }

  @Test
  public void shouldReportWhenValuesExhausted() {
    testee = makeTesteeFor(arbitrary().pick(1).assuming(i -> false));
    testee.check(i -> true);
    verify(reporter, times(1)).valuesExhausted(anyInt());
  }

  @Test
  public void shouldReportNumberOfFoundExamplesWhenValuesExhausted() {
    int numberOfExamples = 3;
    strategy = new Strategy(Configuration.defaultPRNG(0), numberOfExamples, 0, 0, 10,
        reporter, guidance);
    testee = makeTesteeFor(
        arbitrary().pick(1, 2, 1, 1, 1).assuming(
        i -> i == 2));
    testee.check(i -> true);
    verify(reporter, times(1)).valuesExhausted(0);
  }

  @Test
  public void shouldReportNumberOfExamplesUsed() {
    testee = makeTesteeFor(
        arbitrary().pick(0, 1, 2, 3, 4, 5));
    testee.check(i -> i != 4);
    verify(reporter, times(1)).falisification(anyLong(), eq(6), anyInt(),
        anySmallerValues(), any());

  }


  private TheoryRunner<Integer, Integer> makeTesteeFor(
      Gen<Integer> generator) {
    return new TheoryRunner<>(strategy, generator, 
        x -> x, a -> a.toString());
  }

  private List<Object> anySmallerValues() {
    return any(List.class);
  }

}
