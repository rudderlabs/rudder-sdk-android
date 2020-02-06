package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

class RudderServerDestinationDefinition {
    @SerializedName("name")
    String definitionName;
    @SerializedName("displayName")
    String displayName;
    @SerializedName("updatedAt")
    String updatedAt;
}
