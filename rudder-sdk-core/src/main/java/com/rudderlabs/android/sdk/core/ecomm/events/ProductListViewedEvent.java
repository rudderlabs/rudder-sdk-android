package com.rudderlabs.android.sdk.core.ecomm.events;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderlabs.android.sdk.core.util.Utils;

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
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (!TextUtils.isEmpty(listId)) {
            property.put(ECommerceParamNames.LIST_ID, this.listId);
        }
        if (!TextUtils.isEmpty(category)) {
            property.put(ECommerceParamNames.CATEGORY, this.category);
        }
        if (this.products != null && !this.products.isEmpty()) {
            property.put(ECommerceParamNames.PRODUCTS, Utils.convertToList(new Gson().toJson(this.products)));
        }
        return property;
    }
}
