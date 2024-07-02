package com.rudderstack.core.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

/**
 * Configuration of the server
 * @property source
 */
data class RudderServerConfig(
    @Json(name = "isHosted")
    @JsonProperty("isHosted")
    @SerializedName("isHosted")
    val isHosted: Boolean = false,

    @Json(name = "source")
    @JsonProperty("source")
    @SerializedName("source")
    val source: RudderServerConfigSource? = null,
) : Serializable {
    private var _readIsHosted: Boolean? = null
    private var _readSource: RudderServerConfigSource? = null

    companion object {
        @JvmStatic
        private val serialVersionUID: Long = 1
    }

    @Throws(ClassNotFoundException::class, IOException::class)
    private fun readObject(inputStream: ObjectInputStream) {
        _readIsHosted = inputStream.readBoolean()
        _readSource = inputStream.readObject() as? RudderServerConfigSource
    }

    private fun readResolve(): Any {
        return RudderServerConfig(_readIsHosted ?: false, _readSource)
    }

    @Throws(IOException::class)
    private fun writeObject(outputStream: ObjectOutputStream) {
        outputStream.writeBoolean(isHosted)
        outputStream.writeObject(source)
    }


    /**
     * Configuration of source
     *
     * @property sourceId
     * @property sourceName
     * @property isSourceEnabled
     * @property updatedAt
     * @property destinations
     */
    data class RudderServerConfigSource(
        @Json(name = "id")
        @JsonProperty("id")
        @SerializedName("id")
        val sourceId: String? = null,

        @Json(name = "name")
        @JsonProperty("name")
        @SerializedName("name")
        val sourceName: String? = null,

        @Json(name = "enabled")
        @JsonProperty("enabled")
        @SerializedName("enabled")
        val isSourceEnabled: Boolean = false,

        @Json(name = "updatedAt")
        @JsonProperty("updatedAt")
        @SerializedName("updatedAt")
        val updatedAt: String? = null,

        @Json(name = "destinations")
        @JsonProperty("destinations")
        @SerializedName("destinations")
        val destinations: List<RudderServerDestination>? = null,
    ) : Serializable {
        companion object {
            @JvmStatic
            private val serialVersionUID: Long = 2
        }

        private var _readSourceId: String? = null
        private var _readSourceName: String? = null
        private var _readIsSourceEnabled: Boolean? = null
        private var _readUpdatedAt: String? = null
        private var _readDestinations: List<RudderServerDestination>? = null

        @Throws(ClassNotFoundException::class, IOException::class)
        private fun readObject(inputStream: ObjectInputStream) {
            _readSourceId = inputStream.readObject() as? String
            _readSourceName = inputStream.readObject() as? String
            _readIsSourceEnabled = inputStream.readBoolean()
            _readUpdatedAt = inputStream.readObject() as? String
            _readDestinations = inputStream.readObject() as? List<RudderServerDestination>
        }

        private fun readResolve(): Any {
            return RudderServerConfigSource(
                _readSourceId, _readSourceName, _readIsSourceEnabled ?: false, _readUpdatedAt, _readDestinations
            )
        }

        @Throws(IOException::class)
        private fun writeObject(outputStream: ObjectOutputStream) {
            outputStream.writeObject(sourceId)
            outputStream.writeObject(sourceName)
            outputStream.writeBoolean(isSourceEnabled)
            outputStream.writeObject(updatedAt)
            outputStream.writeObject(destinations)
        }
    }

    data class RudderServerDestination(
        @Json(name = "id")
        @JsonProperty("id")
        @SerializedName("id")
        val destinationId: String,

        @Json(name = "name")
        @JsonProperty("name")
        @SerializedName("name")
        val destinationName: String? = null,

        @Json(name = "enabled")
        @JsonProperty("enabled")
        @SerializedName("enabled")
        val isDestinationEnabled: Boolean = false,

        @Json(name = "updatedAt")
        @JsonProperty("updatedAt")
        @SerializedName("updatedAt")
        val updatedAt: String? = null,

        @Json(name = "destinationDefinition")
        @JsonProperty("destinationDefinition")
        @SerializedName("destinationDefinition")
        val destinationDefinition: RudderServerDestinationDefinition? = null,

        @Json(name = "config")
        @JsonProperty("config")
        @SerializedName("config")
        val destinationConfig: Map<String, Any>,

        @JsonProperty("areTransformationsConnected")
        @Json(name = "areTransformationsConnected")
        val areTransformationsConnected: Boolean = false,
    ) : Serializable {
        companion object {
            @JvmStatic
            private val serialVersionUID: Long = 3
        }

        private var _readDestinationId: String? = null
        private var _readDestinationName: String? = null
        private var _readIsDestinationEnabled: Boolean = false
        private var _readUpdatedAt: String? = null
        private var _readDestinationDefinition: RudderServerDestinationDefinition? = null
        private var _readDestinationConfig: Map<String, Any> = mapOf()
        private var _readAreTransformationsConnected: Boolean = false

        @Throws(ClassNotFoundException::class, IOException::class)
        private fun readObject(inputStream: ObjectInputStream) {
            _readDestinationId = inputStream.readObject() as? String
            _readDestinationName = inputStream.readObject() as? String
            _readIsDestinationEnabled = inputStream.readBoolean()
            _readUpdatedAt = inputStream.readObject() as? String
            _readDestinationDefinition = inputStream.readObject() as? RudderServerDestinationDefinition
            _readDestinationConfig = (inputStream.readObject() as? Map<String, Any>) ?: mapOf()
            _readAreTransformationsConnected = inputStream.readBoolean()
        }

        private fun readResolve(): Any {
            return RudderServerDestination(
                _readDestinationId ?: "",
                _readDestinationName,
                _readIsDestinationEnabled,
                _readUpdatedAt,
                _readDestinationDefinition,
                _readDestinationConfig,
                _readAreTransformationsConnected
            )
        }

        @Throws(IOException::class)
        private fun writeObject(outputStream: ObjectOutputStream) {
            outputStream.writeObject(destinationId)
            outputStream.writeObject(destinationName)
            outputStream.writeBoolean(isDestinationEnabled)
            outputStream.writeObject(updatedAt)
            outputStream.writeObject(destinationDefinition)
            outputStream.writeObject(destinationConfig)
            outputStream.writeBoolean(areTransformationsConnected)
        }
    }

    data class RudderServerDestinationDefinition(
        @Json(name = "name")
        @JsonProperty("name")
        @SerializedName("name")
        val definitionName: String? = null,

        @Json(name = "displayName")
        @JsonProperty("displayName")
        @SerializedName("displayName")
        val displayName: String? = null,

        @Json(name = "updatedAt")
        @JsonProperty("updatedAt")
        @SerializedName("updatedAt")
        val updatedAt: String? = null,
    ) : Serializable {
        companion object {
            @JvmStatic
            private val serialVersionUID: Long = 4
        }

        private var _readDefinitionName: String? = null
        private var _readDisplayName: String? = null
        private var _readUpdatedAt: String? = null

        @Throws(ClassNotFoundException::class, IOException::class)
        private fun readObject(inputStream: ObjectInputStream) {
            _readDefinitionName = inputStream.readObject() as? String
            _readDisplayName = inputStream.readObject() as? String
            _readUpdatedAt = inputStream.readObject() as? String
        }

        private fun readResolve(): Any {
            return RudderServerDestinationDefinition(
                _readDefinitionName, _readDisplayName, _readUpdatedAt
            )
        }

        @Throws(IOException::class)
        private fun writeObject(outputStream: ObjectOutputStream) {
            outputStream.writeObject(definitionName)
            outputStream.writeObject(displayName)
            outputStream.writeObject(updatedAt)
        }
    }

}
