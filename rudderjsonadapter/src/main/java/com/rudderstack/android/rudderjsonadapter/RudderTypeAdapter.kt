package com.rudderstack.android.rudderjsonadapter

import java.lang.reflect.ParameterizedType

abstract class RudderTypeAdapter<T>  {
    val type
    get() = (this::class.java.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.get(0)
}