package com.rudderstack.android.models

data class UserSession(
    val lastActiveTimestamp: Long = -1L,
    val sessionId: Long = -1L,
    val isActive: Boolean = false,
    // signifies a new session has started. should be sent as true only once at start
    val sessionStart: Boolean = false
)
