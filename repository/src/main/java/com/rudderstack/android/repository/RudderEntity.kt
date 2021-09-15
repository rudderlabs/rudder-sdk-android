package com.rudderstack.android.repository
@Retention
@Target(AnnotationTarget.CLASS)
annotation class RudderEntity( val tableName : String, val fields : Array<RudderField>)
