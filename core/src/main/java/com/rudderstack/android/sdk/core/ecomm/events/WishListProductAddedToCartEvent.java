package com.rudderstack.android.sdk.core.ecomm.events;

import android.text.TextUtils;

import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceCart;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderstack.android.sdk.core.ecomm.ECommerceProduct;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderstack.android.sdk.core.ecomm.ECommerceWishList;
import com.rudderstack.android.sdk.core.util.Utils;

public class WishListProductAddedToCartEvent extends ECommercePropertyBuilder {
    private ECommerceWishList wishList;

    public WishListProductAddedToCartEvent withWishList(ECommerceWishList wishList) {
        this.wishList = wishList;
        return this;
    }

    private ECommerceProduct product;

    public WishListProductAddedToCartEvent withProduct(ECommerceProduct product) {
        this.product = product;
        return this;
    }

    public WishListProductAddedToCartEvent withProductBuilder(ECommerceProduct.Builder builder) {
        this.product = builder.build();
        return this;
    }

    private String cartId;

    public WishListProductAddedToCartEvent withCartId(String cartId) {
        this.cartId = cartId;
        return this;
    }

    private ECommerceCart cart;

    public WishListProductAddedToCartEvent withCart(ECommerceCart cart) {
        this.cart = cart;
        return this;
    }

    public WishListProductAddedToCartEvent withCartBuilder(ECommerceCart.Builder builder) {
        this.cart = builder.build();
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.WISH_LIST_PRODUCT_ADDED_TO_CART;
    }

    @Override
    public RudderProperty properties() {
        RudderProperty property = new RudderProperty();
        if (this.wishList != null) {
            property.put(ECommerceParamNames.WISHLIST_ID, this.wishList.getWishListId());
            property.put(ECommerceParamNames.WISHLIST_NAME, this.wishList.getWishListName());
        }
        if (this.product != null) {
            property.putValue(Utils.convertToMap(this.product));
        }
        if (!TextUtils.isEmpty(this.cartId)) {
            property.put(ECommerceParamNames.CART_ID, this.cartId);
        } else if (this.cart != null) {
            property.put(ECommerceParamNames.CART_ID, this.cart.getCartId());
        }
        return property;
    }
}
