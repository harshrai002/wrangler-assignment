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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TimeDuration implements Token {
    private final String value;
    private final double timeInNanos;

    public TimeDuration(String value) {
        this.value = value.trim();
        this.timeInNanos = parseToNanos(this.value);
    }

    private double parseToNanos(String value) {
        String lower = value.toLowerCase();
        if (lower.endsWith("ms")) {
            return Double.parseDouble(lower.replace("ms", "")) * 1_000_000;
        } else if (lower.endsWith("s") || lower.endsWith("sec")) {
            return Double.parseDouble(lower.replaceAll("sec|s", "")) * 1_000_000_000;
        } else if (lower.endsWith("m") || lower.endsWith("min")) {
            return Double.parseDouble(lower.replaceAll("min|m", "")) * 60 * 1_000_000_000;
        } else if (lower.endsWith("h") || lower.endsWith("hr") || lower.endsWith("hour")) {
            return Double.parseDouble(lower.replaceAll("h|hr|hour", "")) * 3600 * 1_000_000_000L;
        } else {
            throw new IllegalArgumentException("Unsupported time unit in: " + value);
        }
    }

    public long getNanos() {
        return (long) timeInNanos;
    }


    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TokenType.TIME_DURATION.name());
        json.addProperty("value",value);
        return json;
    }
}