package com.rudderlabs.android.sdk.core.ecomm.events;

import com.google.gson.Gson;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceOrder;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderlabs.android.sdk.core.util.Utils;

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
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (this.order != null) {
            property.put(ECommerceParamNames.ORDER_ID, this.order.getOrderId());
            if (this.value != -1) {
                property.put(ECommerceParamNames.CURRENCY, this.order.getCurrency());
                property.put(ECommerceParamNames.TOTAL, this.value);
            }
        }
        if (this.products != null && !this.products.isEmpty()) {
            property.put(ECommerceParamNames.PRODUCTS, Utils.convertToList(new Gson().toJson(this.products)));
        }
        return property;
    }
}
