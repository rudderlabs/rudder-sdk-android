package com.rudderstack.android.sdk.core.ecomm.events;

import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceProduct;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderstack.android.sdk.core.util.Utils;

public class ProductViewedEvent extends ECommercePropertyBuilder {
    private ECommerceProduct product;

    public ProductViewedEvent withProduct(ECommerceProduct product) {
        this.product = product;
        return this;
    }

    public ProductViewedEvent withProductBuilder(ECommerceProduct.Builder builder) {
        this.product = builder.build();
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.PRODUCT_VIEWED;
    }

    @Override
    public RudderProperty properties() {
        RudderProperty property = new RudderProperty();
        if (this.product != null) {
            property.putValue(Utils.convertToMap(this.product));
        }
        return property;
    }
}
