package com.rudderstack.android.internal.prefs

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import java.io.File

class SharedPrefsStore(
    private val applicationContext: Context,
    prefsName: String,
) : SharedPrefsRepository {

    private val preferences: SharedPreferences = applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    override fun getInt(key: String): Int {
        return preferences.getInt(key, 0)
    }

    override fun getBoolean(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }

    override fun getString(key: String): String {
        return preferences.getString(key, String.empty()) ?: String.empty()
    }

    override fun getLong(key: String): Long {
        return preferences.getLong(key, -1L)
    }

    override fun save(key: String, value: Int) {
        put(key, value)
    }

    override fun save(key: String, value: Boolean) {
        put(key, value)
    }

    override fun save(key: String, value: String) {
        put(key, value)
    }

    override fun save(key: String, value: Long) {
        put(key, value)
    }

    override fun clear(key: String) {
        with(preferences.edit()) {
            remove(key)
            commit()
        }
    }

    override fun deleteSharedPrefs(name: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationContext.deleteSharedPreferences(name)
        } else {
            applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().apply()
            val dir = File(applicationContext.applicationInfo.dataDir, "shared_prefs")
            File(dir, "${name}.xml").delete()
        }
    }

    private fun <T> put(key: String, value: T) {
        put(key, value, preferences)
    }

    private fun <T> put(key: String, value: T, prefs: SharedPreferences) {
        prefs.edit(commit = true) {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is String -> putString(key, value)

                else -> {
                }
            }
        }
    }
}

