package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

public class ProductSearchedEvent extends ECommercePropertyBuilder {
    private String query;

    public ProductSearchedEvent withQuery(String query) {
        this.query = query;
        return this;
    }

    @Override
    public RudderProperty build() throws RudderException {
        if (this.query == null || this.query.isEmpty())
            throw new RudderException("Search query can not be empty");
        RudderProperty property = new RudderProperty();
        property.setProperty(ECommerceParamNames.QUERY, query);
        return property;
    }

    @Override
    public String event() {
        return ECommerceEvents.PRODUCTS_SEARCHED;
    }
}
