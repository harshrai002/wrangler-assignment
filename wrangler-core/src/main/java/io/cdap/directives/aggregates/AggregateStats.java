/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.directives.aggregates;

import com.google.common.collect.ImmutableList;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.EntityCountMetric;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.parser.Expression;
import io.cdap.wrangler.api.parser.Identifier;
import io.cdap.wrangler.api.parser.Numeric;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.expression.EL;
import io.cdap.wrangler.expression.ELContext;
import io.cdap.wrangler.expression.ELException;
import io.cdap.wrangler.expression.ELResult;

import java.util.Collections;
import java.util.List;

import static io.cdap.wrangler.metrics.JexlCategoryMetricUtils.getJexlCategoryMetric;

/**
 * A directive for incrementing the a aggregates variable based on conditions.
 */
@Plugin(type = Directive.TYPE)
@Name(AggregateStats.NAME)
@Categories(categories = { "aggregate"})
@Description("Wrangler - A interactive tool for data cleansing and transformation.")
public class AggregateStats implements Directive {

  public static final String NAME = "aggregate-stats";

  private String sourceSizeColumn;
  private String sourceTimeColumn;
  private String targetSizeColumn;
  private String targetTimeColumn;
  private EL el;

  // Add these instance variables at the class level
  private String sizeUnit = "B"; // default to bytes
  private String timeUnit = "ns"; // default to nanoseconds

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("source-size-column", TokenType.COLUMN_NAME);
    builder.define("source-time-column", TokenType.COLUMN_NAME);
    builder.define("target-size-column", TokenType.COLUMN_NAME);
    builder.define("target-time-column", TokenType.COLUMN_NAME);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.sourceTimeColumn = ((Identifier) args.value("source-time-column")).value();
    this.sourceSizeColumn = ((Identifier) args.value("source-size-column")).value();
    this.targetTimeColumn = ((Identifier) args.value("target-time-column")).value();
    this.targetSizeColumn = ((Identifier) args.value("target-size-column")).value();
  }

  public List<Row> finalize(ExecutorContext context) throws DirectiveExecutionException {
    final String TOTAL_BYTES_KEY = "aggregate-stats.total-bytes";
    final String TOTAL_NANOS_KEY = "aggregate-stats.total-nanos";
    final String COUNT_KEY = "aggregate-stats.count";

    TransientStore store = context.getTransientStore();

    Long totalBytes = (Long) store.get(TOTAL_BYTES_KEY);
    Long totalNanos = (Long) store.get(TOTAL_NANOS_KEY);
    Long count = (Long) store.get(COUNT_KEY);

    if (totalBytes == null) totalBytes = 0L;
    if (totalNanos == null) totalNanos = 0L;
    if (count == null) count = 0L;

    // Convert totals to desired units
    double convertedSize = convertBytes(totalBytes, sizeUnit);
    double convertedTime = convertNanos(totalNanos, timeUnit);

    // Create the final result row
    Row resultRow = new Row();
    resultRow.add(targetSizeColumn, convertedSize);
    resultRow.add(targetTimeColumn, convertedTime);

    return Collections.singletonList(resultRow);
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    // Define constants for transient store keys
      final String TOTAL_BYTES_KEY = "aggregate-stats.total-bytes";
      final String TOTAL_NANOS_KEY = "aggregate-stats.total-nanos";
      final String COUNT_KEY = "aggregate-stats.count";

      TransientStore store = context.getTransientStore();

      // Initialize counters if they don't exist
      if (store.get(TOTAL_BYTES_KEY) == null) {
        store.set(TransientVariableScope.LOCAL, TOTAL_BYTES_KEY, 0L);
      }
      if (store.get(TOTAL_NANOS_KEY) == null) {
        store.set(TransientVariableScope.LOCAL, TOTAL_NANOS_KEY, 0L);
      }
      if (store.get(COUNT_KEY) == null) {
        store.set(TransientVariableScope.LOCAL, COUNT_KEY, 0L);
      }

      // Process each row and accumulate values
      for (Row row : rows) {
        try {
          // Get current values from store
          Long totalBytes = (Long) store.get(TOTAL_BYTES_KEY);
          Long totalNanos = (Long) store.get(TOTAL_NANOS_KEY);
          Long count = (Long) store.get(COUNT_KEY);

          // Get values from current row
          Object sizeValue = row.getValue(sourceSizeColumn);
          Object timeValue = row.getValue(sourceTimeColumn);

          if (sizeValue != null && timeValue != null) {
            // Extract byte size from string value
            String sizeStr = sizeValue.toString();
            long bytes = parseByteSize(sizeStr);

            // Extract time duration from string value
            String timeStr = timeValue.toString();
            long nanos = parseTimeDuration(timeStr);

            // Update accumulated values
            totalBytes += bytes;
            totalNanos += nanos;
            count++;

            // Store updated values
            store.set(TransientVariableScope.LOCAL, TOTAL_BYTES_KEY, totalBytes);
            store.set(TransientVariableScope.LOCAL, TOTAL_NANOS_KEY, totalNanos);
            store.set(TransientVariableScope.LOCAL, COUNT_KEY, count);
          }
        } catch (Exception e) {
          throw new DirectiveExecutionException(NAME, e.getMessage(), e);
        }
      }

      // Create result row with current aggregated values
      Row resultRow = new Row();
      Long totalBytes = (Long) store.get(TOTAL_BYTES_KEY);
      Long totalNanos = (Long) store.get(TOTAL_NANOS_KEY);
      Long count = (Long) store.get(COUNT_KEY);

      if (totalBytes == null) totalBytes = 0L;
      if (totalNanos == null) totalNanos = 0L;
      if (count == null) count = 0L;

      // Add aggregated values to the result row
      resultRow.add(targetSizeColumn, totalBytes);
      resultRow.add(targetTimeColumn, totalNanos);

      // Return the single result row with current aggregates
      return Collections.singletonList(resultRow);
  }

  // Simple byte size parser for strings like "1024B", "10MB", etc.
  private long parseByteSize(String sizeStr) {
    String numeric = sizeStr.replaceAll("[^0-9.]", "");
    String unit = sizeStr.replaceAll("[0-9.]", "").toUpperCase();
    double value = Double.parseDouble(numeric);

    switch (unit) {
      case "KB":
        return (long)(value * 1024);
      case "MB":
        return (long)(value * 1024 * 1024);
      case "GB":
        return (long)(value * 1024 * 1024 * 1024);
      default: // bytes
        return (long)value;
    }
  }

  // Simple time duration parser for strings like "100ms", "5s", etc.
  private long parseTimeDuration(String timeStr) {
    String numeric = timeStr.replaceAll("[^0-9.]", "");
    String unit = timeStr.replaceAll("[0-9.]", "").toLowerCase();
    double value = Double.parseDouble(numeric);

    switch (unit) {
      case "ms":
        return (long)(value * 1_000_000);
      case "s":
      case "seconds":
        return (long)(value * 1_000_000_000);
      case "m":
      case "minutes":
        return (long)(value * 60 * 1_000_000_000);
      default: // nanoseconds
        return (long)value;
    }
  }

  private double convertBytes(long bytes, String unit) {
    switch (unit.toUpperCase()) {
      case "KB":
        return bytes / 1024.0;
      case "MB":
        return bytes / (1024.0 * 1024.0);
      case "GB":
        return bytes / (1024.0 * 1024.0 * 1024.0);
      default: // bytes
        return bytes;
    }
  }

  private double convertNanos(long nanos, String unit) {
    switch (unit.toLowerCase()) {
      case "ms":
        return nanos / 1_000_000.0;
      case "seconds":
        return nanos / 1_000_000_000.0;
      case "minutes":
        return nanos / (60.0 * 1_000_000_000.0);
      default: // nanoseconds
        return nanos;
    }
  }

  public void destroy() {
    // no-op
  }

  public List<EntityCountMetric> getCountMetrics() {
    EntityCountMetric jexlCategoryMetric = getJexlCategoryMetric(el.getScriptParsedText());
    return (jexlCategoryMetric == null) ? null : ImmutableList.of(jexlCategoryMetric);
  }
}