package com.rudderstack.android.sdk.core.ecomm;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;

public class ECommerceOrder {
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("affiliation")
    private String affiliation;
    @SerializedName("total")
    private float total;
    @SerializedName("value")
    private float value;
    @SerializedName("revenue")
    private float revenue;
    @SerializedName("shipping")
    private float shippingCost;
    @SerializedName("tax")
    private float tax;
    @SerializedName("discount")
    private float discount;
    @SerializedName("coupon")
    private String coupon;
    @SerializedName("currency")
    private String currency;
    @SerializedName("products")
    private ArrayList<ECommerceProduct> products;

    public ECommerceOrder(String orderId) {
        this.orderId = orderId;
    }

    public ECommerceOrder(String orderId, String affiliation, float total, float value, float revenue, float shippingCost, float tax, float discount, String coupon, String currency) {
        this.orderId = orderId;
        this.affiliation = affiliation;
        this.total = total;
        this.value = value;
        this.revenue = revenue;
        this.shippingCost = shippingCost;
        this.tax = tax;
        this.discount = discount;
        this.coupon = coupon;
        this.currency = currency;
        this.products = new ArrayList<>();
    }

    public ECommerceOrder(String orderId, String affiliation, float total, float value, float revenue, float shippingCost, float tax, float discount, String coupon, String currency, ArrayList<ECommerceProduct> products) {
        this.orderId = orderId;
        this.affiliation = affiliation;
        this.total = total;
        this.value = value;
        this.revenue = revenue;
        this.shippingCost = shippingCost;
        this.tax = tax;
        this.discount = discount;
        this.coupon = coupon;
        this.currency = currency;
        this.products = products;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getRevenue() {
        return revenue;
    }

    public void setRevenue(float revenue) {
        this.revenue = revenue;
    }

    public float getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(float shippingCost) {
        this.shippingCost = shippingCost;
    }

    public float getTax() {
        return tax;
    }

    public void setTax(float tax) {
        this.tax = tax;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    public String getCoupon() {
        return coupon;
    }

    public void setCoupon(String coupon) {
        this.coupon = coupon;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public ArrayList<ECommerceProduct> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<ECommerceProduct> products) {
        this.products = products;
    }

    public void setProduct(ECommerceProduct product) {
        this.products.add(product);
    }

    public static class Builder {
        private String orderId;

        public Builder withOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        private String affiliation;

        public Builder withAffiliation(String affiliation) {
            this.affiliation = affiliation;
            return this;
        }

        private float total;

        public Builder withTotal(float total) {
            this.total = total;
            return this;
        }

        private float value;

        public Builder withValue(float value) {
            this.value = value;
            return this;
        }

        private float revenue;

        public Builder withRevenue(float revenue) {
            this.revenue = revenue;
            return this;
        }

        private float shippingCost;

        public Builder withShippingCost(float shippingCost) {
            this.shippingCost = shippingCost;
            return this;
        }

        private float tax;

        public Builder withTax(float tax) {
            this.tax = tax;
            return this;
        }

        private float discount;

        public Builder withDiscount(float discount) {
            this.discount = discount;
            return this;
        }

        private String coupon;

        public Builder withCoupon(String coupon) {
            this.coupon = coupon;
            return this;
        }

        private String currency;

        public Builder withCurrency(String currency) {
            this.currency = currency;
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

        public Builder withProducts(ECommerceProduct... products) {
            if (this.products == null) {
                this.products = new ArrayList<>();
            }
            this.products.addAll(Arrays.asList(products));
            return this;
        }

        public Builder withProduct(ECommerceProduct product) {
            if (this.products == null) {
                this.products = new ArrayList<>();
            }
            this.products.add(product);
            return this;
        }

        public ECommerceOrder build() {
            return new ECommerceOrder(
                    orderId,
                    affiliation,
                    total,
                    value,
                    revenue,
                    shippingCost,
                    tax,
                    discount,
                    coupon,
                    currency,
                    products
            );
        }
    }
}
