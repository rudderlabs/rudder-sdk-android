package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

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
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();
        if (this.product == null) throw new RudderException("Product can not be null");
        property.setProperty(this.product);
        if (this.socialChannel != null)
            property.setProperty(ECommerceParamNames.SHARE_VIA, this.socialChannel);
        if (this.shareMessage != null)
            property.setProperty(ECommerceParamNames.SHARE_MESSAGE, this.shareMessage);
        if (this.recipient != null)
            property.setProperty(ECommerceParamNames.RECIPIENT, this.recipient);
        return property;
    }
}
