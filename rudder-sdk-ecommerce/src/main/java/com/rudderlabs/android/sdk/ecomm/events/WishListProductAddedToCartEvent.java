package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceCart;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;
import com.rudderlabs.android.sdk.ecomm.ECommerceWishList;

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
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();
        if (this.wishList == null) throw new RudderException("Wishlist is not initiated");
        property.setProperty(ECommerceParamNames.WISHLIST_ID, this.wishList.getWishListId());
        property.setProperty(ECommerceParamNames.WISHLIST_NAME, this.wishList.getWishListName());
        if (this.product == null) throw new RudderException("Product can not be null");
        property.setProperty(this.product);
        if (this.cart == null && this.cartId == null) throw new RudderException("Cart can't null");
        if (this.cartId != null) property.setProperty(ECommerceParamNames.CART_ID, this.cartId);
        if (this.cart != null)
            property.setProperty(ECommerceParamNames.CART_ID, this.cart.getCartId());
        return property;
    }
}
