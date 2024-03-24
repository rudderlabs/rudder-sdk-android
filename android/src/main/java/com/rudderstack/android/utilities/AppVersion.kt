package com.rudderstack.android.utilities

data class AppVersion(
    val previousVersionCode: Int,
    val previousVersionName: String,
    val currentVersionCode: Int,
    val currentVersionName: String,
) {

    companion object {
        const val DEFAULT_VERSION_CODE = -1
        const val DEFAULT_VERSION_NAME = ""
    }

    fun isApplicationInstalled(): Boolean {
        return previousVersionCode == -1
    }

    fun isApplicationUpdated(): Boolean {
        return previousVersionCode != -1 && previousVersionCode != currentVersionCode
    }
}
