package com.rudderstack.android.models

class RudderContext(contextMap: Map<String, Any?>) : HashMap<String, Any?>(contextMap) {
    constructor() : this(mapOf())

    var app: RudderApp? by this

    var traits: RudderTraits? by this

    var library: RudderLibraryInfo? by this

    var os: RudderOSInfo? by this

    var screen: RudderScreenInfo? by this

    var userAgent: String? by this

    var locale: String? by this

    var device: RudderDeviceInfo? by this

    var network: RudderNetwork? by this

    var timezone: String? by this

    var externalId: MutableSet<Map<String, Any?>>? by this

    var customContextMap: MutableMap<String, Any>? by this
}
