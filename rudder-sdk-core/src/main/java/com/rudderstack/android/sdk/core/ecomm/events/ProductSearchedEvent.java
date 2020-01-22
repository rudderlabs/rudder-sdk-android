package com.rudderstack.android.sdk.core.ecomm.events;

import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;

public class ProductSearchedEvent extends ECommercePropertyBuilder {
    private String query;

    public ProductSearchedEvent withQuery(String query) {
        this.query = query;
        return this;
    }

    @Override
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (this.query != null && !this.query.isEmpty())
            property.put(ECommerceParamNames.QUERY, query);
        return property;
    }

    @Override
    public String event() {
        return ECommerceEvents.PRODUCTS_SEARCHED;
    }
}
