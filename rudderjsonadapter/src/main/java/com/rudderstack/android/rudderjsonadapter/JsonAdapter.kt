package com.rudderstack.android.rudderjsonadapter

import java.lang.reflect.Type

interface JsonAdapter {
    fun <T> writeToJson(/*type: Type*/) : String?
}