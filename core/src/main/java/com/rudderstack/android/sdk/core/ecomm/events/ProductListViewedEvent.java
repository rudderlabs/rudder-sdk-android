package com.rudderstack.android.sdk.core.ecomm.events;

import android.text.TextUtils;

import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderstack.android.sdk.core.ecomm.ECommerceProduct;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderstack.android.sdk.core.gson.RudderGson;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductListViewedEvent extends ECommercePropertyBuilder {
    private String listId;

    public ProductListViewedEvent withListId(String listId) {
        this.listId = listId;
        return this;
    }

    private String category;

    public ProductListViewedEvent withCategory(String category) {
        this.category = category;
        return this;
    }

    private ArrayList<ECommerceProduct> products;

    public ProductListViewedEvent withProducts(List<ECommerceProduct> products) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        this.products.addAll(products);
        return this;
    }

    public ProductListViewedEvent withProduct(ECommerceProduct product) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        this.products.add(product);
        return this;
    }

    public ProductListViewedEvent withProducts(ECommerceProduct... products) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        Collections.addAll(this.products, products);
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.PRODUCT_LIST_VIEWED;
    }

    @Override
    public RudderProperty properties() {
        RudderProperty property = new RudderProperty();
        if (!TextUtils.isEmpty(listId)) {
            property.put(ECommerceParamNames.LIST_ID, this.listId);
        }
        if (!TextUtils.isEmpty(category)) {
            property.put(ECommerceParamNames.CATEGORY, this.category);
        }
        if (this.products != null && !this.products.isEmpty()) {
            property.put(ECommerceParamNames.PRODUCTS, Utils.convertToList(RudderGson.getInstance().toJson(this.products)));
        }
        return property;
    }
}
