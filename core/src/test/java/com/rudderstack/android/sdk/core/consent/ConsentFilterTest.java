package com.rudderstack.android.sdk.core.consent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderMessageBuilder;
import com.rudderstack.android.sdk.core.RudderServerConfigSource;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class ConsentFilterTest {

    private List<ConsentInterceptor> interceptorTestList;
    private ConsentFilter consentFilter;

    private final ConsentInterceptor voidInterceptor = (config, rudderMessage) -> rudderMessage;

    @Before
    public void setup() {
        interceptorTestList = new LinkedList<>();
        consentFilter = new ConsentFilter(interceptorTestList);
    }
    @After
    public void breakdown(){
        interceptorTestList.clear();
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
        assertThat(interceptorTestList, Matchers.emptyCollectionOf(ConsentInterceptor.class));

    }

    @Test
    public void testApplyConsentWithEmptyInterceptorList(){
        RudderMessage rudderMessage = new RudderMessageBuilder().setUserId("u").build();
        RudderMessage updatedMessage = consentFilter.applyConsent(new RudderServerConfigSource(), rudderMessage);

        assertThat(updatedMessage, Matchers.is(rudderMessage));
        assertThat(updatedMessage, Matchers.hasProperty("userId", is("u")));
    }

    @Test
    public void applyConsent() {
        ConsentInterceptor testInterceptor1 = (config, rudderMessage) -> new RudderMessageBuilder()
                .setUserId("u-1").build();
        interceptorTestList.add(testInterceptor1);
        RudderConfig config = new RudderConfig.Builder().build();
        RudderMessage rudderMessage = new RudderMessageBuilder().setUserId("u").build();
        RudderMessage updatedMessage = consentFilter.applyConsent(new RudderServerConfigSource(), rudderMessage);

        assertThat(updatedMessage, not(Matchers.is(rudderMessage)));
        assertThat(updatedMessage, Matchers.hasProperty("userId", is("u-1")));

        ConsentInterceptor testInterceptor2 = (config2, rudderMessage2) -> new RudderMessageBuilder()
                .setUserId(rudderMessage2.getUserId()+"-2").build();
        interceptorTestList.add(testInterceptor2);

        updatedMessage = consentFilter.applyConsent(new RudderServerConfigSource(), rudderMessage);

        assertThat(updatedMessage, not(Matchers.is(rudderMessage)));
        assertThat(updatedMessage, Matchers.hasProperty("userId", is("u-1-2")));

        interceptorTestList.remove(testInterceptor1);
        updatedMessage = consentFilter.applyConsent(new RudderServerConfigSource(), rudderMessage);

        assertThat(updatedMessage, not(Matchers.is(rudderMessage)));
        assertThat(updatedMessage, Matchers.hasProperty("userId", is("u-2")));

    }

}
