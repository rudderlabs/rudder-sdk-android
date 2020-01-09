package com.rudderlabs.android.sdk.core.ecomm;

import com.google.gson.annotations.SerializedName;

public class ECommerceProduct {
    @SerializedName("product_id")
    private String productId;
    @SerializedName("sku")
    private String sku;
    @SerializedName("category")
    private String category;
    @SerializedName("name")
    private String name;
    @SerializedName("brand")
    private String brand;
    @SerializedName("variant")
    private String variant;
    @SerializedName("price")
    private float price;
    @SerializedName("currency")
    private String currency;
    @SerializedName("quantity")
    private float quantity;
    @SerializedName("coupon")
    private String coupon;
    @SerializedName("position")
    private int position;
    @SerializedName("url")
    private String url;
    @SerializedName("image_url")
    private String imageUrl;

    public ECommerceProduct(String productId, String sku, String category, String name, String brand, String variant, float price, String currency, float quantity, String coupon, int position, String url, String imageUrl) {
        this.productId = productId;
        this.sku = sku;
        this.category = category;
        this.name = name;
        this.brand = brand;
        this.variant = variant;
        this.price = price;
        this.currency = currency;
        this.quantity = quantity;
        this.coupon = coupon;
        this.position = position;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getCoupon() {
        return coupon;
    }

    public void setCoupon(String coupon) {
        this.coupon = coupon;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static class Builder {
        private String productId;

        public Builder withProductId(String productId) {
            this.productId = productId;
            return this;
        }

        private String sku;

        public Builder withSku(String sku) {
            this.sku = sku;
            return this;
        }

        private String category;

        public Builder withCategory(String category) {
            this.category = category;
            return this;
        }

        private String name;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        private String brand;

        public Builder withBrand(String brand) {
            this.brand = brand;
            return this;
        }

        private String variant;

        public Builder withVariant(String variant) {
            this.variant = variant;
            return this;
        }

        private float price;

        public Builder withPrice(float price) {
            this.price = price;
            return this;
        }

        private String currency;

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        private float quantity;

        public Builder withQuantity(float quantity) {
            this.quantity = quantity;
            return this;
        }

        private String coupon;

        public Builder withCoupon(String coupon) {
            this.coupon = coupon;
            return this;
        }

        private int position;

        public Builder withPosition(int position) {
            this.position = position;
            return this;
        }

        private String url;

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        private String imageUrl;

        public Builder withImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public ECommerceProduct build() {
            return new ECommerceProduct(
                    productId,
                    sku,
                    category,
                    name,
                    brand,
                    variant,
                    price,
                    currency,
                    quantity,
                    coupon,
                    position,
                    url,
                    imageUrl
            );
        }
    }
}
