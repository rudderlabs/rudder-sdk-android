package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommercePromotion;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

public class PromotionViewedEvent extends ECommercePropertyBuilder {

    private ECommercePromotion promotion;

    public PromotionViewedEvent withPromotion(ECommercePromotion promotion) {
        this.promotion = promotion;
        return this;
    }

    public PromotionViewedEvent withPromotionBuilder(ECommercePromotion.Builder builder) {
        this.promotion = builder.build();
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.PROMOTION_VIEWED;
    }

    @Override
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();

        if (this.promotion == null) {
            throw new RudderException("Promotion can not be empty");
        }
        property.setProperty(this.promotion);

        return property;
    }
}
