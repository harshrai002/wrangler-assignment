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

public class ByteSize implements Token {
    private final String value;
    private final double sizeInBytes;

    public ByteSize(String value) {
        this.value = value.trim();
        this.sizeInBytes = parseToBytes(this.value);
    }

    private double parseToBytes(String value) {
            String lower = value.toLowerCase();
        if (lower.endsWith("kb")) {
            return Double.parseDouble(lower.replace("kb", "")) * 1024;
        } else if (lower.endsWith("mb")) {
            return Double.parseDouble(lower.replace("mb", "")) * 1024 * 1024;
        } else if (lower.endsWith("gb")) {
            return Double.parseDouble(lower.replace("gb", "")) * 1024 * 1024 * 1024;
        } else if (lower.endsWith("tb")) {
            return Double.parseDouble(lower.replace("tb", "")) * 1024L * 1024 * 1024 * 1024;
        } else {
            throw new IllegalArgumentException("Unsupported byte unit in: " + value);
        }
    }

    public long getBytes() {
        return (long) sizeInBytes;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("original", value);
        json.addProperty("bytes", getBytes());
        return json;
    }
}