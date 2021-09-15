package com.rudderstack.android.repository

/**
 * Store Field values. This will be used to create table
 * if isIndex is true, the index name will be "$fieldName1_$fieldName2_idx"
 *
 * @property type
 * @property fieldName
 * @property primaryKey
 * @property isAutoInc only applicable if primary key and type is Integer
 * @property isIndex if this column should serve as a basis for indexing
 *
 */

annotation class RudderField(val type: Type, val fieldName: String, val primaryKey: Boolean = false,
                 val isNullable : Boolean = true,
                 val isAutoInc: Boolean = false, val isIndex: Boolean = false){
    /**
     * Represents type of column
     *
     */
    enum class Type(val notation : String){
        INTEGER("INTEGER"),
        TEXT("TEXT")
    }
}

