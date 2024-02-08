package com.rudderstack.android.sdk.core.gson.gsonadapters;

import static java.lang.Double.POSITIVE_INFINITY;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class DoubleTypeAdapter extends TypeAdapter<Number> {
    @Override
    public Number read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return in.nextDouble();
    }

    @Override
    public void write(JsonWriter out, Number value) throws IOException {
        double doubleValue = value.doubleValue();
        if (Double.isNaN(doubleValue)) {
            out.value("NaN");
        } else if (doubleValue == POSITIVE_INFINITY) {
            out.value("Infinity");
        } else if (doubleValue == Double.NEGATIVE_INFINITY) {
            out.value("-Infinity");
        } else {
            out.value(value);
        }
    }
}