package com.rudderstack.models.android

import com.rudderstack.models.android.RudderProperty
import java.util.HashMap

abstract class RudderProperty {
    private val map: MutableMap<String, Any> = HashMap()
    open fun getMap(): Map<String, Any> {
        return map
    }

    fun hasProperty(key: String): Boolean {
        return map.containsKey(key)
    }

    fun getProperty(key: String): Any? {
        return if (map.containsKey(key)) map[key] else null
    }

    fun put(key: String, value: Any) {
        map[key] = value
    }

    fun putValue(key: String, value: Any): RudderProperty {
        if (value is RudderProperty) {
            map[key] = value.getMap()
        } else {
            map[key] = value
        }
        return this
    }

    fun putValue(map: Map<String, Any>?): RudderProperty {
        if (map != null) this.map.putAll(map)
        return this
    }

    fun putRevenue(revenue: Double) {
        map["revenue"] = revenue
    }

    fun putCurrency(currency: String) {
        map["currency"] = currency
    }
}
data class ScreenProperty(private val screenName : String, private val isAutomatic : Boolean = false) :
    RudderProperty() {
    init {
        putValue("name", screenName)
        putValue("automatic", isAutomatic)
    }
}