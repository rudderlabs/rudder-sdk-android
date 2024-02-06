package com.rudderstack.android.sdk.core.ecomm.events;

import android.text.TextUtils;

import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceFilter;
import com.rudderstack.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderstack.android.sdk.core.ecomm.ECommerceProduct;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderstack.android.sdk.core.ecomm.ECommerceSort;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductListFilteredEvent extends ECommercePropertyBuilder {
    /*
     * list_id
     * */
    private String listId;

    public ProductListFilteredEvent withListId(String listId) {
        this.listId = listId;
        return this;
    }

    /*
     * category
     * */
    private String category;

    public ProductListFilteredEvent withCategory(String category) {
        this.category = category;
        return this;
    }

    /*
     * products
     * */
    private ArrayList<ECommerceProduct> products;

    public ProductListFilteredEvent withProducts(List<ECommerceProduct> products) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        this.products.addAll(products);
        return this;
    }

    public ProductListFilteredEvent withProduct(ECommerceProduct product) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        this.products.add(product);
        return this;
    }

    public ProductListFilteredEvent withProducts(ECommerceProduct... products) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        Collections.addAll(this.products, products);
        return this;
    }

    /*
     * sorts
     * */
    private ArrayList<ECommerceSort> sorts;

    public ProductListFilteredEvent withSorts(List<ECommerceSort> sorts) {
        if (this.sorts == null) this.sorts = new ArrayList<>();
        this.sorts.addAll(sorts);
        return this;
    }

    public ProductListFilteredEvent withSortBuilders(List<ECommerceSort.Builder> builders) throws Exception {
        if (this.sorts == null) this.sorts = new ArrayList<>();
        for (ECommerceSort.Builder builder : builders) {
            this.sorts.add(builder.build());
        }
        return this;
    }

    public ProductListFilteredEvent withSort(ECommerceSort sort) {
        if (this.sorts == null) this.sorts = new ArrayList<>();
        this.sorts.add(sort);
        return this;
    }

    public ProductListFilteredEvent withSortBuilder(ECommerceSort.Builder builder) throws Exception {
        if (this.sorts == null) this.sorts = new ArrayList<>();
        this.sorts.add(builder.build());
        return this;
    }

    public ProductListFilteredEvent withSorts(ECommerceSort... sorts) {
        if (this.sorts == null) this.sorts = new ArrayList<>();
        Collections.addAll(this.sorts, sorts);
        return this;
    }

    public ProductListFilteredEvent withSortBuilders(ECommerceSort.Builder... builders) throws Exception {
        if (this.sorts == null) this.sorts = new ArrayList<>();
        for (ECommerceSort.Builder builder : builders) {
            this.sorts.add(builder.build());
        }
        return this;
    }

    /*
     * filters
     * */
    private ArrayList<ECommerceFilter> filters;

    public ProductListFilteredEvent withFilters(List<ECommerceFilter> filters) {
        if (this.filters == null) this.filters = new ArrayList<>();
        this.filters.addAll(filters);
        return this;
    }

    public ProductListFilteredEvent withFilterBuilders(List<ECommerceFilter.Builder> builders) throws Exception {
        if (this.filters == null) this.filters = new ArrayList<>();
        for (ECommerceFilter.Builder builder : builders) {
            this.filters.add(builder.build());
        }
        return this;
    }

    public ProductListFilteredEvent withFilter(ECommerceFilter filter) {
        if (this.filters == null) this.filters = new ArrayList<>();
        this.filters.add(filter);
        return this;
    }

    public ProductListFilteredEvent withFilterBuilder(ECommerceFilter.Builder builder) throws Exception {
        if (this.filters == null) this.filters = new ArrayList<>();
        this.filters.add(builder.build());
        return this;
    }

    public ProductListFilteredEvent withFilters(ECommerceFilter... filters) {
        if (this.filters == null) this.filters = new ArrayList<>();
        Collections.addAll(this.filters, filters);
        return this;
    }

    public ProductListFilteredEvent withFilterBuilders(ECommerceFilter.Builder... builders) throws Exception {
        if (this.filters == null) this.filters = new ArrayList<>();
        for (ECommerceFilter.Builder builder : builders) {
            this.filters.add(builder.build());
        }
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.PRODUCT_LIST_FILTERED;
    }

    @Override
    public RudderProperty properties() {
        RudderProperty property = new RudderProperty();

        if (!TextUtils.isEmpty(this.listId)) {
            property.put(ECommerceParamNames.LIST_ID, this.listId);
        }
        if (!TextUtils.isEmpty(this.category)) {
            property.put(ECommerceParamNames.CATEGORY, this.category);
        }
        if (this.products != null && !this.products.isEmpty()) {
            property.put(ECommerceParamNames.PRODUCTS, Utils.convertToList(this.products));
        }
        if (this.sorts != null && !this.sorts.isEmpty()) {
            property.put(ECommerceParamNames.SORTS, Utils.convertToList(this.sorts));
        }
        if (this.filters != null && !this.filters.isEmpty()) {
            property.put(ECommerceParamNames.FILTERS, Utils.convertToList(this.filters));
        }

        return property;
    }
}
