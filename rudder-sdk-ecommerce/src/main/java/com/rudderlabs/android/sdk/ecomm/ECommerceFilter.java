package com.rudderlabs.android.sdk.ecomm;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.rudderlabs.android.sdk.core.RudderException;

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

        public ECommerceFilter build() throws RudderException {
            if (TextUtils.isEmpty(this.type)) {
                throw new RudderException("Type can not be empty or null");
            }
            if (TextUtils.isEmpty(this.value)) {
                throw new RudderException("Value can not be empty or null");
            }
            return new ECommerceFilter(this.type, this.value);
        }
    }
}
