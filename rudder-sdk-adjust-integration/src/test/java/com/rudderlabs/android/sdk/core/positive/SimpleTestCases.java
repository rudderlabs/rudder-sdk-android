package com.rudderlabs.android.sdk.core.positive;


import com.rudderlabs.android.sdk.core.*;
import org.junit.Test;


public class SimpleTestCases extends BaseTestCase {

    @Test
    public void testIdentify() throws InterruptedException {
        rudderClient.identify(
                new RudderTraitsBuilder()
                        .setCity("New York")
                        .setCountry("USA")
                        .setPostalCode("ZA22334")
                        .setState("New York")
                        .setStreet("Wall Street")
                        .setAge(25)
                        .setBirthDay("05-09-1997")
                        .setCompanyName("Rudder Labs")
                        .setCompanyId("test--company--id")
                        .setIndustry("Software Engg")
                        .setDescription("Rudder Labs Company")
                        .setEmail("example@gmail.com")
                        .setFirstName("Example")
                        .setGender("Female")
                        .setId("8c3f46c6-2bab-4fa6-b59d-e8d3c8b4045f")
                        .setLastName("Traits")
                        .setName("Example Traits")
                        .setPhone("9876543212")
                        .setTitle("Mrs")
                        .setUserName("example_traits")
                        .build()
        );
        rudderClient.flush();
        Thread.sleep(2000);
    }

    @Test
    public void testSimpleTrackEvent() throws InterruptedException {
        // track event
        try {
            RudderElement pageViewEvent = new RudderMessageBuilder()
                    .setEventName("Test Track")
                    .setProperty(new TrackPropertyBuilder()
                            .setCategory("Test Category")
                            .setLabel("Test Label")
                            .setValue("Test Value"))
                    .build();
            rudderClient.track(pageViewEvent);
            rudderClient.flush();
            Thread.sleep(2000);
        } catch (RudderException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimplePageViewEvent() throws InterruptedException {
        // page view event
        try {
            RudderElement pageViewEvent = new RudderMessageBuilder()
                    .setProperty(new PagePropertyBuilder()
                            .setUrl("http://jsonviewer.stack.hu")
                            .setKeywords("Test")
                            .setPath("http://jsonviewer.stack.hu")
                            .setReferrer("Test Event")
                            .setTitle("Test Title")
                            .setSearch("Test"))
                    .build();

            rudderClient.page(pageViewEvent);
            rudderClient.flush();
            Thread.sleep(2000);
        } catch (RudderException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSimpleScreenViewEvent() throws InterruptedException {
        // screen view event
        try {
            RudderElement screenViewEvent = new RudderMessageBuilder()
                    .setProperty(new ScreenPropertyBuilder()
                            .setScreenName("Test Screen"))
                    .build();
            rudderClient.screen(screenViewEvent);
            rudderClient.flush();
            Thread.sleep(2000);
        } catch (RudderException e) {
            e.printStackTrace();
        }
    }
}