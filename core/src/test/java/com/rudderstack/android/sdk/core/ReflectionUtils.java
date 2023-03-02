package com.rudderstack.android.sdk.core;

import java.lang.reflect.Field;

public class ReflectionUtils {
    public static <T> int getInt(T object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(object);
    }
    public static <T> long getLong(T object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getLong(object);
    }
    public static <T> String getString(T object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(object);
    }
    public static <T, R> R getObject(T object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (R) field.get(object);
    }

}
