package com.rudderstack.android.repository

import android.content.ContentValues

interface Entity {



    /**
     * Maintain same sequence as fields
     *
     * @return
     */
    fun generateContentValues() : ContentValues
    fun nullHackColumn() : String? = null

    /**
     * returns the values associated with the primary key
     *
     * @return for example if there's a single primary key, return arrayOf(id.toString())
     * else return arrayOf(id.toString(), email)
     */
    fun getPrimaryKeyValues() : Array<String>

}