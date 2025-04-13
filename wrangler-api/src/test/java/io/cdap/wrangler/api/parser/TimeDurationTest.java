/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.cdap.wrangler.api.parser;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link TimeDuration} class to verify parsing and value retrieval.
 */
public class TimeDurationTest {

  @Test
  public void testMillisecondParsing() {
    TimeDuration duration = new TimeDuration("5ms");
    Assert.assertEquals(5 * 1_000_000, duration.getNanos());
    Assert.assertEquals("5ms", duration.value());
    Assert.assertEquals(TokenType.TIME_DURATION, duration.type());
  }

  @Test
  public void testSecondParsing() {
    TimeDuration duration = new TimeDuration("2.1s");
    Assert.assertEquals((long) (2.1 * 1_000_000_000), duration.getNanos());
    Assert.assertEquals("2.1s", duration.value());
  }

  @Test
  public void testSecondWithUnitParsing() {
    TimeDuration duration = new TimeDuration("3.5sec");
    Assert.assertEquals((long) (3.5 * 1_000_000_000), duration.getNanos());
    Assert.assertEquals("3.5sec", duration.value());
  }

  @Test
  public void testMinuteParsing() {
    TimeDuration duration = new TimeDuration("1.5m");
    Assert.assertEquals((long) (1.5 * 60 * 1_000_000_000), duration.getNanos());
    Assert.assertEquals("1.5m", duration.value());
  }

  @Test
  public void testMinuteWithUnitParsing() {
    TimeDuration duration = new TimeDuration("2.5min");
    Assert.assertEquals((long) (2.5 * 60 * 1_000_000_000), duration.getNanos());
    Assert.assertEquals("2.5min", duration.value());
  }

  @Test
  public void testHourParsing() {
    TimeDuration duration = new TimeDuration("1.25h");
    Assert.assertEquals((long) (1.25 * 3600 * 1_000_000_000), duration.getNanos());
    Assert.assertEquals("1.25h", duration.value());
  }

  @Test
  public void testCaseInsensitivity() {
    TimeDuration durationLower = new TimeDuration("30ms");
    TimeDuration durationMixed = new TimeDuration("30Ms");
    
    Assert.assertEquals(durationLower.getNanos(), durationMixed.getNanos());
  }

  @Test
  public void testWhitespaceHandling() {
    TimeDuration duration1 = new TimeDuration("10s");
    TimeDuration duration2 = new TimeDuration("10s ");
    
    Assert.assertEquals(duration1.getNanos(), duration2.getNanos());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new TimeDuration("10invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingUnit() {
    new TimeDuration("10");
  }

  @Test(expected = NumberFormatException.class)
  public void testInvalidNumber() {
    new TimeDuration("abc ms");
  }
}
