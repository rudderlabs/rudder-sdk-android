/*
 * Creator: Debanjan Chatterjee on 09/06/23, 5:31 pm Last modified: 06/06/23, 1:04 pm
 * Copyright: All rights reserved 2023 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.android.ruddermetricsreporterandroid.error;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.rudderstack.android.ruddermetricsreporterandroid.Logger;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DateUtils;
import com.squareup.moshi.Json;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kotlin.jvm.Transient;

@SuppressWarnings("ConstantConditions")
public class Breadcrumb {

    // non-private to allow direct field access optimizations
    private String name;
    private BreadcrumbType type = BreadcrumbType.MANUAL;
    private Map<String, Object> metadata = new HashMap<>();
    @JsonIgnore
    @Transient
    @Json(ignore = true)
    private final Date timestamp;
    @JsonProperty("timestamp")
    @SerializedName("timestamp")
    @Json(name = "timestamp")
    private final String timestampString;
    @JsonIgnore
    @Transient
    @Json(ignore = true)
    private final Logger logger;


    Breadcrumb(@NonNull String message, @NonNull Logger logger) {
        this.name = message;
        this.logger = logger;
        this.timestamp = new Date();
        timestampString = DateUtils.toIso8601(timestamp);
    }

    Breadcrumb(@NonNull String message,
               @NonNull BreadcrumbType type,
               @Nullable Map<String, Object> metadata,
               @NonNull Date timestamp,
               @NonNull Logger logger) {
        this.logger = logger;
        this.name = message;
        this.type = type;
        this.metadata = metadata;
        this.timestamp = timestamp;
        timestampString = DateUtils.toIso8601(timestamp);
    }

    private void logNull(String property) {
        logger.e("Invalid null value supplied to breadcrumb." + property + ", ignoring");
    }

    /**
     * Sets the description of the breadcrumb
     */
    public void setName(@NonNull String name) {
        if (name != null) {
            this.name = name;
        } else {
            logNull("message");
        }
    }

    /**
     * Gets the description of the breadcrumb
     */
    @NonNull
    public String getName() {
        return name;
    }


    public void setType(@NonNull BreadcrumbType type) {
        if (type != null) {
            this.type = type;
        } else {
            logNull("type");
        }
    }


    @NonNull
    public BreadcrumbType getType() {
        return type;
    }

    /**
     * Sets diagnostic data relating to the breadcrumb
     */
    public void setMetadata(@Nullable Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets diagnostic data relating to the breadcrumb
     */
    @Nullable
    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    /**
     * The timestamp that the breadcrumb was left
     */
    @NonNull
    public Date getTimestamp() {
        return this.timestamp;
    }

    @NonNull
    String getStringTimestamp() {
        return timestampString;
    }

    @NonNull
    @Override
    public String toString() {
        return "Breadcrumb{" +
                "message='" + name + '\'' +
                ", type=" + type +
                ", metadata=" + metadata +
                ", timestamp=" + timestamp +
                '}';
    }
}
