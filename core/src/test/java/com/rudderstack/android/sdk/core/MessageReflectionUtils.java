package com.rudderstack.android.sdk.core;

public class MessageReflectionUtils {
    public static String getMessageId(RudderMessage message) throws NoSuchFieldException, IllegalAccessException {
        return ReflectionUtils.getString(message, "messageId");
    }
    public static String getTimestamp(RudderMessage message) throws NoSuchFieldException, IllegalAccessException {
        return ReflectionUtils.getString(message, "timestamp");
    }
}
