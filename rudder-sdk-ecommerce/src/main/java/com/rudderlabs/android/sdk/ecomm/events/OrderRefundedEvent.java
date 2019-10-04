package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceOrder;
import com.rudderlabs.android.sdk.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderRefundedEvent extends ECommercePropertyBuilder {
    private ECommerceOrder order;

    public OrderRefundedEvent withOrder(ECommerceOrder order) {
        this.order = order;
        return this;
    }

    public OrderRefundedEvent withOrderBuilder(ECommerceOrder.Builder builder) {
        this.order = builder.build();
        return this;
    }

    private ArrayList<ECommerceProduct> products;

    public OrderRefundedEvent withProduct(ECommerceProduct product) {
        if (this.products == null) this.products = new ArrayList<>();
        this.products.add(product);
        return this;
    }

    public OrderRefundedEvent withProducts(List<ECommerceProduct> products) {
        if (this.products == null) this.products = new ArrayList<>();
        this.products.addAll(products);
        return this;
    }

    public OrderRefundedEvent withProducts(ECommerceProduct... products) {
        if (this.products == null) this.products = new ArrayList<>();
        Collections.addAll(this.products, products);
        return this;
    }

    private float value = -1;

    public OrderRefundedEvent withRefundValue(float value) {
        this.value = value;
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.ORDER_REFUNDED;
    }

    @Override
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();
        if (this.order == null) throw new RudderException("Order can't be null");
        property.setProperty(ECommerceParamNames.ORDER_ID, this.order.getOrderId());
        if (this.value != -1) {
            property.setProperty(ECommerceParamNames.CURRENCY, this.order.getCurrency());
            property.setProperty(ECommerceParamNames.TOTAL, this.value);
        }
        if (this.products != null && !this.products.isEmpty()) {
            property.setProperty(ECommerceParamNames.PRODUCTS, this.products);
        }
        return property;
    }
}
