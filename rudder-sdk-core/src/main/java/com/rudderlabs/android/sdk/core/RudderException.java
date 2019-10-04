package com.rudderlabs.android.sdk.core;

/*
* wrapper around Exception
* */
public class RudderException extends Exception {
    public RudderException(String message) {
        super(message);
    }
}
