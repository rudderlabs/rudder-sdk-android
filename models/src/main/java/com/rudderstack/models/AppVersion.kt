package com.rudderstack.models

data class AppVersion(
    val previousVersionCode: Int,
    val previousVersionName: String,
    val currentVersionCode: Int,
    val currentVersionName: String,
)
