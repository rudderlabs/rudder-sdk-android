package com.rudderlabs.android.sdk.core;

import com.google.gson.annotations.SerializedName;

class TraitsCompany {
    @SerializedName("name")
    private String name;
    @SerializedName("id")
    private String id;
    @SerializedName("industry")
    private String industry;

    TraitsCompany(String name, String id, String industry) {
        this.name = name;
        this.id = id;
        this.industry = industry;
    }
}
