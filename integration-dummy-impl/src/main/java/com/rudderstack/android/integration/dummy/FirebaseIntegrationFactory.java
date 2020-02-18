package com.rudderstack.android.integration.dummy;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FirebaseIntegrationFactory extends RudderIntegration<FirebaseAnalytics> {
    private static final String FIREBASE_KEY = "Firebase";
    private static FirebaseAnalytics _firebaseAnalytics;

    private static final List<String> GOOGLE_RESERVED_KEYWORDS = Arrays.asList(
            "age", "gender", "interest"
    );

    private static final List<String> RESERVED_PARAM_NAMES = Arrays.asList(
            "product_id", "name", "category", "quantity", "price", "currency",
            "value", "order_id", "tax", "shipping", "coupon"
    );

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(@Nullable Object settings, @NonNull RudderClient client, @NonNull RudderConfig rudderConfig) {
            RudderLogger.logDebug("Creating RudderIntegrationFactory");
            return new FirebaseIntegrationFactory(settings, client, rudderConfig);
        }

        @Override
        public String key() {
            return FIREBASE_KEY;
        }
    };

    private FirebaseIntegrationFactory(@Nullable Object config, @NonNull RudderClient client, @NonNull RudderConfig rudderConfig) {
        if (client.getApplication() != null) {
            RudderLogger.logDebug("Initializing Firebase SDK");
            _firebaseAnalytics = FirebaseAnalytics.getInstance(client.getApplication());
        }
    }

    private void processRudderEvent(@NonNull RudderMessage element) {
        if (element.getType() != null && _firebaseAnalytics != null) {
            switch (element.getType()) {
                case MessageType.IDENTIFY:
                    if (!TextUtils.isEmpty(element.getUserId())) {
                        RudderLogger.logDebug("Setting userId to Firebase");
                        _firebaseAnalytics.setUserId(element.getUserId());
                    }
                    Map<String, Object> traits = element.getTraits();
                    for (String key : traits.keySet()) {
                        key = key.toLowerCase().trim().replace(" ", "_");
                        if (key.length() > 40) {
                            key = key.substring(0, 40);
                        }
                        if (!GOOGLE_RESERVED_KEYWORDS.contains(key)) {
                            RudderLogger.logDebug("Setting userProperties to Firebase");
                            _firebaseAnalytics.setUserProperty(key, new Gson().toJson(traits.get(key)));
                        }
                    }
                    break;
                case MessageType.SCREEN:
                    RudderLogger.logInfo("Rudder doesn't support screen calls for Firebase Native SDK mode as screen recording in Firebase works out of the box");
                    break;
                case MessageType.TRACK:
                    String eventName = element.getEventName();
                    if (!TextUtils.isEmpty(eventName)) {
                        String firebaseEvent;
                        Bundle params = null;
                        switch (eventName) {
                            case ECommerceEvents.PAYMENT_INFO_ENTERED:
                                firebaseEvent = FirebaseAnalytics.Event.ADD_PAYMENT_INFO;
                                break;
                            case ECommerceEvents.PRODUCT_ADDED:
                                firebaseEvent = FirebaseAnalytics.Event.ADD_TO_CART;
                                params = new Bundle();
                                this.addProductProperties(params, element.getProperties());
                                break;
                            case ECommerceEvents.PRODUCT_ADDED_TO_WISH_LIST:
                                firebaseEvent = FirebaseAnalytics.Event.ADD_TO_WISHLIST;
                                params = new Bundle();
                                this.addProductProperties(params, element.getProperties());
                                break;
                            case "Application Opened":
                                firebaseEvent = FirebaseAnalytics.Event.APP_OPEN;
                                break;
                            case ECommerceEvents.CHECKOUT_STARTED:
                                firebaseEvent = FirebaseAnalytics.Event.BEGIN_CHECKOUT;
                                params = new Bundle();
                                this.addOrderProperties(params, element.getProperties());
                                break;
                            case ECommerceEvents.ORDER_COMPLETED:
                                firebaseEvent = FirebaseAnalytics.Event.ECOMMERCE_PURCHASE;
                                params = new Bundle();
                                this.addOrderProperties(params, element.getProperties());
                                break;
                            case ECommerceEvents.ORDER_REFUNDED:
                                firebaseEvent = FirebaseAnalytics.Event.PURCHASE_REFUND;
                                params = new Bundle();
                                this.addOrderProperties(params, element.getProperties());
                                break;
                            case ECommerceEvents.PRODUCTS_SEARCHED:
                                firebaseEvent = FirebaseAnalytics.Event.SEARCH;
                                params = new Bundle();
                                this.addSearchProperties(params, element.getProperties());
                                break;
                            case ECommerceEvents.PRODUCT_SHARED:
                                firebaseEvent = FirebaseAnalytics.Event.SHARE;
                                params = new Bundle();
                                this.addShareProperties(params, element.getProperties());
                                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "product");
                                break;
                            case ECommerceEvents.CART_SHARED:
                                firebaseEvent = FirebaseAnalytics.Event.SHARE;
                                params = new Bundle();
                                this.addShareProperties(params, element.getProperties());
                                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "cart");
                                break;
                            case ECommerceEvents.PRODUCT_VIEWED:
                                firebaseEvent = FirebaseAnalytics.Event.VIEW_ITEM;
                                params = new Bundle();
                                this.addProductProperties(params, element.getProperties());
                                break;
                            case ECommerceEvents.PRODUCT_LIST_VIEWED:
                                firebaseEvent = FirebaseAnalytics.Event.VIEW_ITEM_LIST;
                                params = new Bundle();
                                this.addProductListProperty(params, element.getProperties());
                                break;
                            case ECommerceEvents.PRODUCT_REMOVED:
                                firebaseEvent = FirebaseAnalytics.Event.REMOVE_FROM_CART;
                                params = new Bundle();
                                this.addProductProperties(params, element.getProperties());
                                break;
                            case ECommerceEvents.CHECKOUT_STEP_VIEWED:
                                firebaseEvent = FirebaseAnalytics.Event.CHECKOUT_PROGRESS;
                                params = new Bundle();
                                this.addCheckoutProperties(params, element.getProperties());
                                break;
                            case ECommerceEvents.PRODUCT_CLICKED:
                                firebaseEvent = FirebaseAnalytics.Event.SELECT_CONTENT;
                                params = new Bundle();
                                this.addProductProperties(params, element.getProperties());
                                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "product");
                                break;
                            case ECommerceEvents.PROMOTION_VIEWED:
                                firebaseEvent = FirebaseAnalytics.Event.PRESENT_OFFER;

                                break;
                            default:
                                // log custom event
                                firebaseEvent = eventName.toLowerCase().trim().replace(" ", "_");
                                if (firebaseEvent.length() > 40) {
                                    firebaseEvent = firebaseEvent.substring(0, 40);
                                }
                        }
                        if (!TextUtils.isEmpty(firebaseEvent)) {
                            if (params == null) {
                                params = new Bundle();
                            }
                            this.attachCustomProperties(params, element.getProperties());
                            RudderLogger.logDebug("Logged \"" + firebaseEvent + "\" to Firebase");
                            _firebaseAnalytics.logEvent(firebaseEvent, params);
                        }
                    }
                    break;
                default:
                    RudderLogger.logInfo("MessageType is not supported through " + FIREBASE_KEY);
                    break;
            }
        }
    }

    private void addCheckoutProperties(Bundle params, Map<String, Object> properties) {
        if (params != null && properties != null) {
            if (properties.containsKey("step")) {
                String step = (String) properties.get("step");
                params.putInt(FirebaseAnalytics.Param.CHECKOUT_STEP, Integer.parseInt(step != null ? step : "0"));
            }
        }
    }

    private void addProductListProperty(Bundle params, Map<String, Object> properties) {
        if (params != null && properties != null) {
            if (properties.containsKey("category")) {
                params.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, (String) properties.get("category"));
            }
        }
    }

    private void addShareProperties(Bundle params, Map<String, Object> properties) {
        if (params != null && properties != null) {
            if (properties.containsKey("cart_id")) {
                params.putString(FirebaseAnalytics.Param.ITEM_ID, (String) properties.get("cart_id"));
            } else if (properties.containsKey("product_id")) {
                params.putString(FirebaseAnalytics.Param.ITEM_ID, (String) properties.get("product_id"));
            }
            if (properties.containsKey("share_via")) {
                params.putString(FirebaseAnalytics.Param.METHOD, (String) properties.get("share_via"));
            }
        }
    }

    private void addSearchProperties(Bundle params, Map<String, Object> properties) {
        if (params != null && properties != null) {
            if (properties.containsKey("query")) {
                params.putString(FirebaseAnalytics.Param.SEARCH_TERM, (String) properties.get("query"));
            }
        }
    }

    private void addOrderProperties(Bundle params, Map<String, Object> properties) {
        if (params != null && properties != null) {
            try {
                if (properties.containsKey("value")) {
                    String value = (String) properties.get("value");
                    params.putFloat(FirebaseAnalytics.Param.VALUE, Float.parseFloat(value != null ? value : "0"));
                }
                if (properties.containsKey("currency")) {
                    params.putString(FirebaseAnalytics.Param.CURRENCY, (String) properties.get("currency"));
                } else {
                    params.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
                }
                if (properties.containsKey("order_id")) {
                    params.putString(FirebaseAnalytics.Param.TRANSACTION_ID, (String) properties.get("order_id"));
                }
                if (properties.containsKey("tax")) {
                    String tax = (String) properties.get("tax");
                    params.putFloat(FirebaseAnalytics.Param.TAX, Float.parseFloat(tax != null ? tax : "0"));
                }
                if (properties.containsKey("shipping")) {
                    String shipping = (String) properties.get("shipping");
                    params.putFloat(FirebaseAnalytics.Param.SHIPPING, Float.parseFloat(shipping != null ? shipping : "0"));
                }
                if (properties.containsKey("coupon")) {
                    params.putString(FirebaseAnalytics.Param.COUPON, (String) properties.get("coupon"));
                }
            } catch (Exception ex) {
                RudderLogger.logError(ex);
            }
        }
    }

    private void addProductProperties(Bundle params, Map<String, Object> properties) {
        if (properties != null && params != null) {
            try {
                if (properties.containsKey("product_id")) {
                    params.putString(FirebaseAnalytics.Param.ITEM_ID, (String) properties.get("product_id"));
                }
                if (properties.containsKey("name")) {
                    params.putString(FirebaseAnalytics.Param.ITEM_NAME, (String) properties.get("name"));
                }
                if (properties.containsKey("category")) {
                    params.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, (String) properties.get("category"));
                }
                if (properties.containsKey("quantity")) {
                    String quantity = (String) properties.get("quantity");
                    params.putLong(FirebaseAnalytics.Param.QUANTITY, Long.parseLong(quantity != null ? quantity : "0"));
                }
                if (properties.containsKey("price")) {
                    String price = (String) properties.get("price");
                    params.putLong(FirebaseAnalytics.Param.PRICE, Long.parseLong(price != null ? price : "0"));
                }
                if (properties.containsKey("currency")) {
                    params.putString(FirebaseAnalytics.Param.CURRENCY, (String) properties.get("currency"));
                } else {
                    params.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
                }
            } catch (Exception ex) {
                RudderLogger.logError(ex);
            }
        }
    }

    private void attachCustomProperties(Bundle params, Map<String, Object> properties) {
        if (properties != null) {
            for (String key : properties.keySet()) {
                if (!RESERVED_PARAM_NAMES.contains(key)) {
                    Object value = properties.get(key);
                    if (value != null) {
                        if (value instanceof Boolean) {
                            params.putBoolean(key, (Boolean) value);
                        } else if (value instanceof Integer) {
                            params.putInt(key, (Integer) value);
                        } else if (value instanceof Long) {
                            params.putLong(key, (Long) value);
                        } else if (value instanceof Double) {
                            params.putDouble(key, (Double) value);
                        } else if (value instanceof String) {
                            String val = (String) value;
                            if (val.length() > 100) val = val.substring(0, 100);
                            params.putString(key, val);
                        } else {
                            params.putString(key, new Gson().toJson(value));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        // Firebase doesn't support reset functionality
    }

    @Override
    public void dump(@Nullable RudderMessage element) {
        if (element != null) {
            processRudderEvent(element);
        }
    }

    @Override
    public FirebaseAnalytics getUnderlyingInstance() {
        return _firebaseAnalytics;
    }
}
