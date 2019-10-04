package com.rudderlabs.android.sdk.core;

import com.google.gson.annotations.SerializedName;

class TraitsAddress {
    @SerializedName("rl_city")
    private String city;
    @SerializedName("rl_country")
    private String country;
    @SerializedName("rl_postalcode")
    private String postalCode;
    @SerializedName("rl_state")
    private String state;
    @SerializedName("rl_street")
    private String street;

    TraitsAddress(String city, String country, String postalCode, String state, String street) {
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.state = state;
        this.street = street;
    }
}
