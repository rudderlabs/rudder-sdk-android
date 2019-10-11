package com.rudderlabs.android.sdk.core;

import android.text.TextUtils;

public class TrackPropertyBuilder extends RudderPropertyBuilder {
    private String category = null;

    public TrackPropertyBuilder setCategory(String category) {
        this.category = category;
        return this;
    }

    private String label = "";

    public TrackPropertyBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    private String value = "";

    public TrackPropertyBuilder setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (TextUtils.isEmpty(category)) {
            RudderLogger.logError("category can not be null or empty");
        } else {
            property.setProperty("category", this.category);
            property.setProperty("label", this.label);
            property.setProperty("value", this.value);
        }
        return property;
    }
}
