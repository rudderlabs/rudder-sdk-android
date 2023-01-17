package com.rudderstack.android.sdk.core.consent;

import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderMessage;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class ConsentFilter {
    private Collection<Interceptor> interceptorList;

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

    public void applyConsent(RudderConfig config, RudderMessage message){

    }

}
