package com.rudderstack.android.repository

interface EntityFactory {
    /**
     * Create entity for given class.
     * An eg on how to compare classes
     * val sc : Class<*> = CharSequence::class.java
     * when(sc){
     *   String ::class ->{
     *       }
     * }
     *
     * @param T the type of object required
     * @param entity The class sent as T type is erased
     * @param values The values defined by the annotation @RudderEntity
     * @see RudderEntity
     * @return an object of T
     */
    fun <T : Entity> getEntity(entity:Class<T>, values: Map<String, Any>) : T
}