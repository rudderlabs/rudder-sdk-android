package com.rudderstack.android.sdk.core.gson.gsonadapters;

import android.renderscript.Type;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class FloatTypeAdapter extends TypeAdapter<Number> {
    @Override
    public Number read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return (float) in.nextDouble();
    }

    @Override
    public void write(JsonWriter out, Number value) throws IOException {
        float floatValue = value.floatValue();
        if (Float.isNaN(floatValue)) {
            out.value("NaN");
        } else if (floatValue == Float.POSITIVE_INFINITY) {
            out.value("Infinity");
        } else if (floatValue == Float.NEGATIVE_INFINITY) {
            out.value("-Infinity");
        } else {
            out.value(value);
        }
    }
}