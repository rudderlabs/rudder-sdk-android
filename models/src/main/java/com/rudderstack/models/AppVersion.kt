package com.rudderstack.models

data class AppVersion(
    val previousBuild: Int,
    val previousVersionName: String,
    val currentBuild: Int,
    val currentVersionName: String,
)
