package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;
import com.rudderlabs.android.sdk.ecomm.ECommerceWishList;

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
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();
        if (this.wishList == null) throw new RudderException("Wishlist is not initiated");
        property.setProperty(ECommerceParamNames.WISHLIST_ID, this.wishList.getWishListId());
        property.setProperty(ECommerceParamNames.WISHLIST_NAME, this.wishList.getWishListName());
        if (this.product == null) throw new RudderException("Product can not be null");
        property.setProperty(this.product);
        return property;
    }
}
