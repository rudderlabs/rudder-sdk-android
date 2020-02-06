package com.rudderstack.android.sdk.core;

import android.text.TextUtils;

public class ScreenPropertyBuilder extends RudderPropertyBuilder {
    private String name;

    public ScreenPropertyBuilder setScreenName(String name) {
        this.name = name;
        return this;
    }

    private boolean isAutomatic = false;
    ScreenPropertyBuilder isAtomatic(boolean isAutomatic) {
        this.isAutomatic = isAutomatic;
        return this;
    }

    @Override
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (TextUtils.isEmpty(name)) {
            RudderLogger.logError("name can not be empty");
        } else {
            property.put("name", name);
        }
        property.put("automatic", isAutomatic);
        return property;
    }
}
