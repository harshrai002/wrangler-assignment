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
 * Tests for {@link ByteSize} class to verify parsing and value retrieval.
 */
public class ByteSizeTest {

  @Test
  public void testKilobyteParsing() {
    ByteSize byteSize = new ByteSize("10kb");
    Assert.assertEquals(10 * 1024, byteSize.getBytes());
    Assert.assertEquals("10kb", byteSize.value());
    Assert.assertEquals(TokenType.BYTE_SIZE, byteSize.type());
  }

  @Test
  public void testMegabyteParsing() {
    ByteSize byteSize = new ByteSize("1.5MB");
    Assert.assertEquals((long) (1.5 * 1024 * 1024), byteSize.getBytes());
    Assert.assertEquals("1.5MB", byteSize.value());
  }

  @Test
  public void testGigabyteParsing() {
    ByteSize byteSize = new ByteSize("2.25GB");
    Assert.assertEquals((long) (2.25 * 1024 * 1024 * 1024), byteSize.getBytes());
    Assert.assertEquals("2.25GB", byteSize.value());
  }

  @Test
  public void testTerabyteParsing() {
    ByteSize byteSize = new ByteSize("0.5TB");
    Assert.assertEquals((long) (0.5 * 1024 * 1024 * 1024 * 1024), byteSize.getBytes());
    Assert.assertEquals("0.5TB", byteSize.value());
  }

  @Test
  public void testCaseInsensitivity() {
    ByteSize byteSizeLower = new ByteSize("5kb");
    ByteSize byteSizeUpper = new ByteSize("5KB");
    ByteSize byteSizeMixed = new ByteSize("5Kb");
    
    Assert.assertEquals(byteSizeLower.getBytes(), byteSizeUpper.getBytes());
    Assert.assertEquals(byteSizeLower.getBytes(), byteSizeMixed.getBytes());
  }

  @Test
  public void testWhitespaceHandling() {
    ByteSize byteSize1 = new ByteSize("10kb");
    ByteSize byteSize2 = new ByteSize("10kb ");
    
    Assert.assertEquals(byteSize1.getBytes(), byteSize2.getBytes());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new ByteSize("10invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingUnit() {
    new ByteSize("10");
  }

  @Test(expected = NumberFormatException.class)
  public void testInvalidNumber() {
    new ByteSize("abc kb");
  }
}
