package com.rudderlabs.android.sdk.core;

import android.text.TextUtils;

public class ScreenPropertyBuilder extends RudderPropertyBuilder {
    private String name;

    public ScreenPropertyBuilder setScreenName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (TextUtils.isEmpty(name)) {
            RudderLogger.logError(new RudderException("name can not be empty"));
        } else {
            property.setProperty("name", name);
        }
        return property;
    }
}
