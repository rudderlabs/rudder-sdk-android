package com.rudderlabs.android.sdk.core.ecomm;

import com.google.gson.annotations.SerializedName;

public class ECommerceWishList {
    @SerializedName("wishlist_id")
    private String wishListId;
    @SerializedName("wishlist_name")
    private String wishListName;

    public ECommerceWishList(String wishListId, String wishListName) {
        this.wishListId = wishListId;
        this.wishListName = wishListName;
    }

    public String getWishListId() {
        return wishListId;
    }

    public void setWishListId(String wishListId) {
        this.wishListId = wishListId;
    }

    public String getWishListName() {
        return wishListName;
    }

    public void setWishListName(String wishListName) {
        this.wishListName = wishListName;
    }

    public static class Builder {
        private String wishListId;

        public Builder withWishListId(String wishListId) {
            this.wishListId = wishListId;
            return this;
        }

        private String wishListName;

        public Builder withWishListName(String wishListName) {
            this.wishListName = wishListName;
            return this;
        }

        public ECommerceWishList build() {
            return new ECommerceWishList(wishListId, wishListName);
        }
    }
}
