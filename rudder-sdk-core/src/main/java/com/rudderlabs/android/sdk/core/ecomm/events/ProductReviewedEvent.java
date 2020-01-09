package com.rudderlabs.android.sdk.core.ecomm.events;

import android.text.TextUtils;

import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.core.ecomm.ECommercePropertyBuilder;

import org.w3c.dom.Text;

public class ProductReviewedEvent extends ECommercePropertyBuilder {
    private ECommerceProduct product;

    public ProductReviewedEvent withProduct(ECommerceProduct product) {
        this.product = product;
        return this;
    }

    public ProductReviewedEvent withProductBuilder(ECommerceProduct.Builder builder) {
        this.product = builder.build();
        return this;
    }

    private String reviewId;

    public ProductReviewedEvent withReviewId(String reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    private String reviewBody;

    public ProductReviewedEvent withReviewBody(String reviewBody) {
        this.reviewBody = reviewBody;
        return this;
    }

    private double rating;

    public ProductReviewedEvent withRating(double rating) {
        this.rating = rating;
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.PRODUCT_REVIEWED;
    }

    @Override
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (this.product != null) {
            property.put(ECommerceParamNames.PRODUCT_ID, this.product.getProductId());
        }
        if (!TextUtils.isEmpty(this.reviewId)) {
            property.put(ECommerceParamNames.REVIEW_ID, this.reviewId);
        }
        if (!TextUtils.isEmpty(reviewBody)) {
            property.put(ECommerceParamNames.REVIEW_BODY, this.reviewBody);
        }
        property.put(ECommerceParamNames.RATING, this.rating);
        return property;
    }
}
