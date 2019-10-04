package com.rudderlabs.android.sdk.ecomm;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ECommerceCart {
    @SerializedName("cart_id")
    private String cartId;
    @SerializedName("products")
    private ArrayList<ECommerceProduct> products;

    public ECommerceCart(String cartId, ArrayList<ECommerceProduct> products) {
        this.cartId = cartId;
        this.products = products;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public ArrayList<ECommerceProduct> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<ECommerceProduct> products) {
        this.products = products;
    }

    public ECommerceCart(String cartId) {
        this.cartId = cartId;
    }

    public static class Builder {
        private String cartId;

        public Builder withCartId(String cartId) {
            this.cartId = cartId;
            return this;
        }

        private ArrayList<ECommerceProduct> products;

        public Builder withProducts(ArrayList<ECommerceProduct> products) {
            if (this.products == null) {
                this.products = new ArrayList<>();
            }
            this.products.addAll(products);
            return this;
        }

        public Builder withProduct(ECommerceProduct product) {
            if (this.products == null) {
                this.products = new ArrayList<>();
            }
            this.products.add(product);
            return this;
        }

        public ECommerceCart build() {
            return new ECommerceCart(cartId, products);
        }
    }
}
