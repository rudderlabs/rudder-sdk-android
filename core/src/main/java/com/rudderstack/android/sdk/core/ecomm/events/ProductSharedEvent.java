package com.rudderstack.android.sdk.core.ecomm.events;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderstack.android.sdk.core.ecomm.ECommerceProduct;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderstack.android.sdk.core.util.Utils;

public class ProductSharedEvent extends ECommercePropertyBuilder {
    private ECommerceProduct product;

    public ProductSharedEvent withProduct(ECommerceProduct product) {
        this.product = product;
        return this;
    }

    public ProductSharedEvent withProductBuilder(ECommerceProduct.Builder builder) {
        this.product = builder.build();
        return this;
    }

    private String socialChannel;

    public ProductSharedEvent withSocialChannel(String socialChannel) {
        this.socialChannel = socialChannel;
        return this;
    }

    private String shareMessage;

    public ProductSharedEvent withShareMessage(String shareMessage) {
        this.shareMessage = shareMessage;
        return this;
    }

    private String recipient;

    public ProductSharedEvent withRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.PRODUCT_SHARED;
    }

    @Override
    public RudderProperty properties() {
        RudderProperty property = new RudderProperty();
        if (this.product != null) {
            property.putValue(Utils.convertToMap(new Gson().toJson(this.product)));
        }
        if (!TextUtils.isEmpty(this.socialChannel)) {
            property.put(ECommerceParamNames.SHARE_VIA, this.socialChannel);
        }
        if (!TextUtils.isEmpty(this.shareMessage)) {
            property.put(ECommerceParamNames.SHARE_MESSAGE, this.shareMessage);
        }
        if (!TextUtils.isEmpty(this.recipient)) {
            property.put(ECommerceParamNames.RECIPIENT, this.recipient);
        }
        return property;
    }
}
