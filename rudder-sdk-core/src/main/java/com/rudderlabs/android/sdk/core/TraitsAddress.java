package com.rudderlabs.android.sdk.core;

import com.google.gson.annotations.SerializedName;

class TraitsAddress {
    @SerializedName("city")
    private String city;
    @SerializedName("country")
    private String country;
    @SerializedName("postalcode")
    private String postalCode;
    @SerializedName("state")
    private String state;
    @SerializedName("street")
    private String street;

    TraitsAddress(String city, String country, String postalCode, String state, String street) {
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.state = state;
        this.street = street;
    }
}
