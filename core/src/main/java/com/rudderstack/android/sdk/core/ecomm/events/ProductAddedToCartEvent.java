package com.rudderstack.android.sdk.core.ecomm.events;

import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderstack.android.sdk.core.ecomm.ECommerceProduct;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderstack.android.sdk.core.util.Utils;

public class ProductAddedToCartEvent extends ECommercePropertyBuilder {
    private String cartId;

    public ProductAddedToCartEvent withCartId(String cartId) {
        this.cartId = cartId;
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.PRODUCT_ADDED;
    }

    private ECommerceProduct product;

    public ProductAddedToCartEvent withProduct(ECommerceProduct product) {
        this.product = product;
        return this;
    }

    public ProductAddedToCartEvent withProductBuilder(ECommerceProduct.Builder builder) {
        this.product = builder.build();
        return this;
    }

    @Override
    public RudderProperty properties() {
        RudderProperty property = new RudderProperty();
        if (this.product != null) {
            property.putValue(Utils.convertToMap(this.product));
        }
        if (this.cartId != null) {
            property.put(ECommerceParamNames.CART_ID, this.cartId);
        }
        return property;
    }
}
