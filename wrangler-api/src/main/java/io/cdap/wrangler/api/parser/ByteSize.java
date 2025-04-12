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