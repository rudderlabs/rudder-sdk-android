package com.rudderstack.android.rudderjsonadapter

import java.lang.reflect.Type

abstract class JsonAdapterFactory {
    abstract fun getAdapter() : JsonAdapter
    fun test(){
//        val ja = object :JsonAdapter{
//            override fun <T> writeToJson(type: Type): String? {
//                TODO("Not yet implemented")
//            }
//        }
//        ja.writeToJson<Map<String, String>>()
    }
}