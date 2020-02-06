package com.rudderstack.android.sdk.core.ecomm;

import com.rudderstack.android.sdk.core.RudderProperty;

public abstract class ECommercePropertyBuilder{
    public abstract String event();
    public abstract RudderProperty properties();
}
