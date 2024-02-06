package com.rudderstack.android.sdk.core.ecomm.events;

import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommercePromotion;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderstack.android.sdk.core.util.Utils;

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
    public RudderProperty properties() {
        RudderProperty property = new RudderProperty();
        if (this.promotion != null) {
            property.putValue(Utils.convertToMap(this.promotion));
        }
        return property;
    }
}
