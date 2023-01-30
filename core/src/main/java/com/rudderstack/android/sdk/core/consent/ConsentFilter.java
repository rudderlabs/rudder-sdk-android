package com.rudderstack.android.sdk.core.consent;

import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderServerConfigSource;

import java.util.Collection;
import java.util.LinkedList;

public final class ConsentFilter {
    private final Collection<ConsentInterceptor> interceptorList;

    public ConsentFilter(Collection<ConsentInterceptor> interceptorList) {
        synchronized (this) {
            this.interceptorList = interceptorList;
        }
    }

    public ConsentFilter() {
        this(new LinkedList<>());
    }

    public void addInterceptor(ConsentInterceptor interceptor){
        synchronized (this) {
            interceptorList.add(interceptor);
        }
    }
    public void removeInterceptor(ConsentInterceptor interceptor){
        synchronized (this) {
            interceptorList.remove(interceptor);
        }
    }
    public void removeAllInterceptors(){
        synchronized (this) {
            interceptorList.clear();
        }
    }

    public RudderMessage applyConsent(RudderServerConfigSource config, RudderMessage message){
        synchronized (this) {
            if (interceptorList.isEmpty())
                return message;
        }
        return runInterceptorsOnMessage(message, config, interceptorList);
    }

    private RudderMessage runInterceptorsOnMessage(RudderMessage message, RudderServerConfigSource config, Collection<ConsentInterceptor> interceptorList) {
        RudderMessage updatedMessage = message;
        synchronized (this) {
            for (ConsentInterceptor interceptor : interceptorList) {
                updatedMessage = interceptor.intercept(config, updatedMessage);
            }
        }
        return updatedMessage;
    }

}
