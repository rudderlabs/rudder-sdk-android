package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

public class ProductRemovedEvent extends ECommercePropertyBuilder {
    private ECommerceProduct product;

    public ProductRemovedEvent withProduct(ECommerceProduct product) {
        this.product = product;
        return this;
    }

    public ProductRemovedEvent withProductBuilder(ECommerceProduct.Builder builder) {
        this.product = builder.build();
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.PRODUCT_REMOVED;
    }

    @Override
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();
        if (this.product == null) throw new RudderException("Product can not be null");
        property.setProperty(this.product);
        return property;
    }
}
