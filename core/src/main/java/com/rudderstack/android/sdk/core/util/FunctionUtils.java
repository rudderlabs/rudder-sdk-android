package com.rudderstack.android.sdk.core.util;

import java.io.IOException;

public class FunctionUtils {
    @FunctionalInterface
    public interface Function<T, R> {

        /**
         * Applies this function to the given argument.
         *
         * @param t the function argument
         * @return the function result
         */
        R apply(T t) throws IOException;
    }
}
