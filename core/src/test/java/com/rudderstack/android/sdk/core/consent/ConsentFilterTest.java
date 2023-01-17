package com.rudderstack.android.sdk.core.consent;

import static org.hamcrest.MatcherAssert.assertThat;

import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderMessageBuilder;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class ConsentFilterTest {

    private List<Interceptor> interceptorTestList;
    private ConsentFilter consentFilter;

    private final Interceptor voidInterceptor = (config, rudderMessage) -> {};

    @Before
    public void setup(){
        interceptorTestList = new LinkedList<>();
        consentFilter = new ConsentFilter(interceptorTestList);
    }

    @Test
    public void addInterceptor() {
        int interceptorListPrevSize = interceptorTestList.size();
        consentFilter.addInterceptor(voidInterceptor);
        assertThat(interceptorTestList.size(), Matchers.is(interceptorListPrevSize + 1));
    }

    @Test
    public void removeInterceptor() {

        int interceptorListPrevSize = interceptorTestList.size();
        for (int i = 0; i < 3; i++) {
            consentFilter.addInterceptor(voidInterceptor);
        }
        assertThat(interceptorTestList.size(), Matchers.is(interceptorListPrevSize + 3));

        consentFilter.removeInterceptor(voidInterceptor);
        assertThat(interceptorTestList.size(), Matchers.is(interceptorListPrevSize + 2));
        consentFilter.removeInterceptor(voidInterceptor);
        assertThat(interceptorTestList.size(), Matchers.is(interceptorListPrevSize + 1));
    }

    @Test
    public void removeAllInterceptors() {

        int interceptorListPrevSize = interceptorTestList.size();
        for (int i = 0; i < 3; i++) {
            consentFilter.addInterceptor(voidInterceptor);
        }
        assertThat(interceptorTestList.size(), Matchers.is(interceptorListPrevSize + 3));

        consentFilter.removeAllInterceptors();
        assertThat(interceptorTestList, Matchers.emptyCollectionOf(Interceptor.class));

    }


    @Test
    public void applyConsent() {
        Interceptor testInterceptor1 = (config, rudderMessage) -> {
//            rudderMessage.getIntegrations();
        };
    }

}
