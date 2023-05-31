package com.rudderstack.android.ruddermetricsreporterandroid.internal;

public class Intrinsics {
    private Intrinsics() {
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}
