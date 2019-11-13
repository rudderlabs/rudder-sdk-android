[ ![Download](https://api.bintray.com/packages/rudderlabs-bintray/rudder-sdk-android-core/rudder-sdk-android-core/images/download.svg?version=0.1.1) ](https://bintray.com/rudderlabs-bintray/rudder-sdk-android-core/rudder-sdk-android-core/0.1.1/link)

# What is Rudder?

**Short answer:** 
Rudder is an open-source Segment alternative written in Go, built for the enterprise. .

**Long answer:** 
Rudder is a platform for collecting, storing and routing customer event data to dozens of tools. Rudder is open-source, can run in your cloud environment (AWS, GCP, Azure or even your data-centre) and provides a powerful transformation framework to process your event data on the fly.

Released under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

## Getting Started with Android SDK

1. Add these lines to your ```app/build.gradle```
```
repositories {
  maven {
    jcenter()
  }
}
```
2. Add the dependency under ```dependencies```
```
implementation 'com.rudderlabs.android.sdk:rudder-sdk-core:0.1.1'
```

## Initialize ```RudderClient```
```
val rudderClient: RudderClient = RudderClient.getInstance(
    this,
    WRITE_KEY,
    RudderConfig.Builder()
        .withEndPointUri(END_POINT_URI)
        .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
        .build()
)
```
or (compatible with existing segment code)
```
RudderClient.Builder builder = new RudderClient.Builder(this, WRITE_KEY);
builder.logLevel(RudderLogger.RudderLogLevel.VERBOSE);
RudderClient.setSingletonInstance(builder.build());
```

## Send Events
```
rudderClient.track(
    RudderMessageBuilder()
        .setEventName("some_custom_event")
        .setProperty(
            TrackPropertyBuilder()
                .setCategory("test_category")
                .build()
        )
        .setUserId("test_user_id")
)
```
or (compatible with existing segment instrumentation code)
```
String customEvent = "some_custom_event";
String propertyKey = "some_property_key";
String propertyValue = "some_property_value";
RudderClient.with(this).track(
        customEvent,
        new RudderProperty().putValue(propertyKey, propertyValue)
);
```

# Coming Soon

1. Native platform SDK integration support
2. More documentation
3. More destination support
