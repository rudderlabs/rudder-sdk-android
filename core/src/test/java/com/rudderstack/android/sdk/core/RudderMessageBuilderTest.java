package com.rudderstack.android.sdk.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Collections;

public class RudderMessageBuilderTest {

    private final RudderMessage testMessage = new RudderMessageBuilder()
            .setEventName("e-1")
            .setGroupId("g-id")
            .setPreviousId("p-id")
            .setProperty(Collections.singletonMap("s-id", "some-prop"))
            .build();
    @Test
    public void testBuildWithoutParentMsg() throws NoSuchFieldException, IllegalAccessException {

        assertThat(testMessage.getEventName(), is("e-1"));
        assertThat(testMessage.getGroupId(), is("g-id"));
        assertThat(ReflectionUtils.getString(testMessage, "previousId"), is("p-id"));
        assertThat(testMessage.getProperties(), hasEntry("s-id", "some-prop"));
    }

    @Test
    public void testBuildWithParentMsg() throws NoSuchFieldException, IllegalAccessException {
        final RudderMessage copiedMessage = RudderMessageBuilder.from(testMessage)
                .setEventName("copy-1")
                .setProperty(Collections.singletonMap("c-id", "some-copy-prop"))
                .build();


        assertThat(copiedMessage.getEventName(), is("copy-1"));
        assertThat(copiedMessage.getGroupId(), is("g-id"));
        assertThat(ReflectionUtils.getString(copiedMessage, "previousId"), is("p-id"));
        assertThat(copiedMessage.getProperties(), hasEntry("c-id", "some-copy-prop"));

    }
}