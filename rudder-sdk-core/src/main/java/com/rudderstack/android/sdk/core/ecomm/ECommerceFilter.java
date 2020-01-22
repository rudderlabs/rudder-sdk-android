package com.rudderstack.android.sdk.core.ecomm;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class ECommerceFilter {
    @SerializedName("type")
    private String type;
    @SerializedName("value")
    private String value;

    public ECommerceFilter(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static class Builder {
        /*
         * type
         * */
        private String type;

        public ECommerceFilter.Builder withType(String type) {
            this.type = type;
            return this;
        }

        /*
         * value
         * */
        private String value;

        public ECommerceFilter.Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public ECommerceFilter build() throws Exception {
            if (TextUtils.isEmpty(this.type)) {
                throw new Exception("Type can not be empty or null");
            }
            if (TextUtils.isEmpty(this.value)) {
                throw new Exception("Value can not be empty or null");
            }
            return new ECommerceFilter(this.type, this.value);
        }
    }
}
