package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

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
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();
        if (this.product == null) throw new RudderException("Product can not be null");
        property.setProperty(this.product);
        if (this.cartId == null) throw new RudderException("Cart ID can not be null");
        property.setProperty(ECommerceParamNames.CART_ID, this.cartId);
        return property;
    }
}
