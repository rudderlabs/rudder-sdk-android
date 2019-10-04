package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.ecomm.ECommerceProduct;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

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

    private String rating;

    public ProductReviewedEvent withRating(String rating) {
        this.rating = rating;
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.PRODUCT_REVIEWED;
    }

    @Override
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();
        if (this.product == null) throw new RudderException("Product can't be null");
        property.setProperty(ECommerceParamNames.PRODUCT_ID, this.product.getProductId());
        if (this.reviewId != null)
            property.setProperty(ECommerceParamNames.REVIEW_ID, this.reviewId);
        if (this.reviewBody != null)
            property.setProperty(ECommerceParamNames.REVIEW_BODY, this.reviewBody);
        if (this.rating != null) property.setProperty(ECommerceParamNames.RATING, this.rating);
        return property;
    }
}
