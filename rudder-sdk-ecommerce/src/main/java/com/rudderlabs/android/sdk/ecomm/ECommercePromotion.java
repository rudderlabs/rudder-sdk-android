package com.rudderlabs.android.sdk.ecomm;

import com.google.gson.annotations.SerializedName;

public class ECommercePromotion {
    @SerializedName("promotion_id")
    private String promotionId;
    @SerializedName("creative")
    private String creative;
    @SerializedName("name")
    private String name;
    @SerializedName("position")
    private String position;

    public ECommercePromotion(String promotionId, String creative, String name, String position) {
        this.promotionId = promotionId;
        this.creative = creative;
        this.name = name;
        this.position = position;
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public String getCreative() {
        return creative;
    }

    public void setCreative(String creative) {
        this.creative = creative;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public static class Builder {
        private String promotionId;

        public Builder withPromotionId(String promotionId) {
            this.promotionId = promotionId;
            return this;
        }

        private String creative;

        public Builder withCreative(String creative) {
            this.creative = creative;
            return this;
        }

        private String name;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        private String position;

        public Builder withPosition(String position) {
            this.position = position;
            return this;
        }

        public ECommercePromotion build() {
            return new ECommercePromotion(
                    promotionId,
                    creative,
                    name,
                    position
            );
        }
    }
}
