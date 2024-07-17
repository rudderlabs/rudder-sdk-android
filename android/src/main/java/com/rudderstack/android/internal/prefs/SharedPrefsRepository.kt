package com.rudderstack.android.internal.prefs

interface SharedPrefsRepository {
    fun getInt(key: String): Int
    fun getBoolean(key: String): Boolean
    fun getString(key: String): String
    fun getLong(key: String): Long
    fun save(key: String, value: Int)
    fun save(key: String, value: Boolean)
    fun save(key: String, value: String)
    fun save(key: String, value: Long)
    fun clear(key: String)
    fun deleteSharedPrefs(name: String): Boolean
}
