package com.rudderlabs.android.sdk.core;

import com.google.gson.annotations.SerializedName;

class TraitsCompany {
    @SerializedName("rl_name")
    private String name;
    @SerializedName("rl_id")
    private String id;
    @SerializedName("rl_industry")
    private String industry;

    TraitsCompany(String name, String id, String industry) {
        this.name = name;
        this.id = id;
        this.industry = industry;
    }
}
