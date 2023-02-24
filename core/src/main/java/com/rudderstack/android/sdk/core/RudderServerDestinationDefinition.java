package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RudderServerDestinationDefinition implements Serializable {
    @SerializedName("name")
    String definitionName;
    @SerializedName("displayName")
    String displayName;
    @SerializedName("updatedAt")
    String updatedAt;

    public String getDefinitionName() {
        return definitionName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
