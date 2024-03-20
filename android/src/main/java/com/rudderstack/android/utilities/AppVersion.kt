package com.rudderstack.android.utilities

data class AppVersion(
    val previousBuild: Int,
    val previousVersionName: String,
    val currentBuild: Int,
    val currentVersionName: String,
) {

    companion object {
        const val DEFAULT_BUILD = -1
        const val DEFAULT_VERSION_NAME = ""
    }

    fun isApplicationInstalled(): Boolean {
        return previousBuild == -1
    }

    fun isApplicationUpdated(): Boolean {
        return previousBuild != -1 && previousBuild != currentBuild
    }
}
