package com.rudderstack.android.ruddermetricsreporterandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.rudderstack.android.ruddermetricsreporterandroid.internal.BreadcrumbInternal;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DateUtils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public class Breadcrumb {

    // non-private to allow direct field access optimizations
    private String message;
    private BreadcrumbType type = BreadcrumbType.MANUAL;
    private Map<String, Object> metadata = new HashMap<>();
    private Date timestamp = new Date();
    private final Logger logger;


    Breadcrumb(@NonNull String message, @NonNull Logger logger) {
        this.message = message;
        this.logger = logger;
    }

    Breadcrumb(@NonNull String message,
               @NonNull BreadcrumbType type,
               @Nullable Map<String, Object> metadata,
               @NonNull Date timestamp,
               @NonNull Logger logger) {
        this.logger = logger;
        this.message = message;
        this.type = type;
        this.metadata = metadata;
        this.timestamp = timestamp;
    }

    private void logNull(String property) {
        logger.e("Invalid null value supplied to breadcrumb." + property + ", ignoring");
    }

    /**
     * Sets the description of the breadcrumb
     */
    public void setMessage(@NonNull String message) {
        if (message != null) {
            this.message = message;
        } else {
            logNull("message");
        }
    }

    /**
     * Gets the description of the breadcrumb
     */
    @NonNull
    public String getMessage() {
        return message;
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
        return DateUtils.toIso8601(this.timestamp);
    }

    @NonNull
    @Override
    public String toString() {
        return "Breadcrumb{" +
                "message='" + message + '\'' +
                ", type=" + type +
                ", metadata=" + metadata +
                ", timestamp=" + timestamp +
                '}';
    }
}
