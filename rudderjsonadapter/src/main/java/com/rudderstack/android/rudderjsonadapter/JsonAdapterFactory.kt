package com.rudderstack.android.rudderjsonadapter

import java.lang.reflect.Type

abstract class JsonAdapterFactory {
    abstract fun getAdapter() : JsonAdapter

}