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
    public RudderProperty build() throws RudderException {
        if (TextUtils.isEmpty(category)) {
            throw new RudderException("category can not be null or empty");
        }

        RudderProperty property = new RudderProperty();
        property.setProperty("category", this.category);
        property.setProperty("label", this.label);
        property.setProperty("value", this.value);

        return property;
    }
}
