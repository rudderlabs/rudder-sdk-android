package com.rudderstack.android.core.network

import java.io.UnsupportedEncodingException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

object RudderResponseUtils {
    // range constants
    const val MIN_CONFIG_REFRESH_INTERVAL = 1
    const val MAX_CONFIG_REFRESH_INTERVAL = 24
    const val MIN_SLEEP_TIMEOUT = 10
    const val MIN_FLUSH_QUEUE_SIZE = 1
    const val MAX_FLUSH_QUEUE_SIZE = 100
    const val MAX_EVENT_SIZE = 32 * 1024 // 32 KB
    const val MAX_BATCH_SIZE = 500 * 1024 // 500 KB
    val timeZone: String
        get() {
            val timeZone = TimeZone.getDefault()
            return timeZone.id
        }
    val timeStamp: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("GMT")
            return formatter.format(Date())
        }

    fun toDateString(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return date.let {
            formatter.format(it)
        }
    }

    /*fun getDeviceId(application: Application): String {
        if (Build.VERSION.SDK_INT >= 17) {
            val androidId =
                Settings.System.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
            if (!TextUtils.isEmpty(androidId)
                && "9774d56d682e549c" != androidId
                && "unknown" != androidId
                && "000000000000000" != androidId
            ) {
                return androidId
            }
        }

        // If this still fails, generate random identifier that does not persist across installations
        return UUID.randomUUID().toString()
    }

    fun getWriteKeyFromStrings(context: Context): String? {
        val id = context.resources.getIdentifier(
            context.packageName,
            "string",
            "rudder_write_key"
        )
        return if (id != 0) {
            context.resources.getString(id)
        } else {
            null
        }
    }*/

    fun getUTF8Length(message: String): Int {
        return try {
            message.toByteArray(charset("UTF-8")).size
        } catch (ex: UnsupportedEncodingException) {
//            RudderLogger.logError(ex);
            -1
        }
    }

    fun getUTF8Length(message: StringBuilder): Int {
        return getUTF8Length(message.toString())
    }

    fun isOnClassPath(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    enum class NetworkResponses {
        SUCCESS, ERROR, WRITE_KEY_ERROR
    }
}