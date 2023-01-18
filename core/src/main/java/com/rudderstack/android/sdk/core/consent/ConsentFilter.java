package com.rudderstack.android.sdk.core.consent;

import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderMessage;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class ConsentFilter {
    private final Collection<Interceptor> interceptorList;

    public ConsentFilter(Collection<Interceptor> interceptorList) {
        this.interceptorList = interceptorList;
    }

    public ConsentFilter() {
        this(new LinkedList<Interceptor>());
    }

    public void addInterceptor(Interceptor interceptor){
        interceptorList.add(interceptor);
    }
    public void removeInterceptor(Interceptor interceptor){
        interceptorList.remove(interceptor);
    }
    public void removeAllInterceptors(){
        interceptorList.clear();
    }

    public RudderMessage applyConsent(RudderConfig config, RudderMessage message){
        if(interceptorList.isEmpty())
            return message;
        return runInterceptorsOnMessage(message, config, interceptorList);
    }

    private RudderMessage runInterceptorsOnMessage(RudderMessage message, RudderConfig config, Collection<Interceptor> interceptorList) {
        RudderMessage updatedMessage = message;
        for (Interceptor interceptor: interceptorList) {
            updatedMessage = interceptor.intercept(config, updatedMessage);
        }
        return updatedMessage;
    }

}
