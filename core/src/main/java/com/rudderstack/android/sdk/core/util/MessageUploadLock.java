package com.rudderstack.android.sdk.core.util;

public class MessageUploadLock {
    public static final Object UPLOAD_LOCK = new Object();
    public static final Object DEVICE_TRANSFORMATION_LOCK = new Object();
    public static final Object REQUEST_LOCK = new Object();
    public static final Object REDIS_TRANSFORM_LOCK = new Object();
}
