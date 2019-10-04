package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceCart;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartSharedEvent extends ECommercePropertyBuilder {
    private ECommerceCart cart;

    public CartSharedEvent withCart(ECommerceCart cart) {
        this.cart = cart;
        return this;
    }

    public CartSharedEvent withCartBuilder(ECommerceCart.Builder builder) {
        this.cart = builder.build();
        return this;
    }

    private String socialChannel;

    public CartSharedEvent withSocialChannel(String socialChannel) {
        this.socialChannel = socialChannel;
        return this;
    }

    private String shareMessage;

    public CartSharedEvent withShareMessage(String shareMessage) {
        this.shareMessage = shareMessage;
        return this;
    }

    private String recipient;

    public CartSharedEvent withRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.CART_SHARED;
    }

    @Override
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();
        if (this.cart == null) throw new RudderException("Cart can't be null");

        property.setProperty(ECommerceParamNames.CART_ID, this.cart.getCartId());

        ArrayList<Map<String, String>> productIds = new ArrayList<>();
        for (ECommerceProduct product : this.cart.getProducts()) {
            Map<String, String> productIdMap = new HashMap<>();
            productIdMap.put(ECommerceParamNames.PRODUCT_ID, product.getProductId());
            productIds.add(productIdMap);
        }

        property.setProperty(ECommerceParamNames.PRODUCTS, productIds);

        if (this.socialChannel != null)
            property.setProperty(ECommerceParamNames.SHARE_VIA, this.socialChannel);
        if (this.shareMessage != null)
            property.setProperty(ECommerceParamNames.SHARE_MESSAGE, this.shareMessage);
        if (this.recipient != null)
            property.setProperty(ECommerceParamNames.RECIPIENT, this.recipient);
        return property;
    }
}
