package com.rudderlabs.android.sdk.core.ecomm.events;

import com.google.gson.Gson;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceWishList;
import com.rudderlabs.android.sdk.core.util.Utils;

public class ProductRemovedFromWishListEvent extends ECommercePropertyBuilder {
    private ECommerceWishList wishList;

    public ProductRemovedFromWishListEvent withWishList(ECommerceWishList wishList) {
        this.wishList = wishList;
        return this;
    }

    private ECommerceProduct product;

    public ProductRemovedFromWishListEvent withProduct(ECommerceProduct product) {
        this.product = product;
        return this;
    }

    public ProductRemovedFromWishListEvent withProductBuilder(ECommerceProduct.Builder builder) {
        this.product = builder.build();
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.PRODUCT_REMOVED_FROM_WISH_LIST;
    }

    @Override
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (this.wishList != null) {
            property.put(ECommerceParamNames.WISHLIST_ID, this.wishList.getWishListId());
            property.put(ECommerceParamNames.WISHLIST_NAME, this.wishList.getWishListName());
        }
        if (this.product != null) {
            property.putValue(Utils.convertToMap(new Gson().toJson(this.product)));
        }
        return property;
    }
}
