# What is Rudder?

**Short answer:** 
Rudder is an open-source Segment alternative written in Go, built for the enterprise. .

**Long answer:** 
Rudder is a platform for collecting, storing and routing customer event data to dozens of tools. Rudder is open-source, can run in your cloud environment (AWS, GCP, Azure or even your data-centre) and provides a powerful transformation framework to process your event data on the fly.

Released under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

# Why Rudder ?

We are building Rudder because we believe open-source and cloud-prem is important for three main reasons

1. **Privacy & Security:** You should be able to collect and store your customer data without sending everything to a 3rd party vendor or embedding proprietary SDKs. With Rudder, the event data is always in your control. Besides, Rudder gives you fine-grained control over what data to forward to what analytical tool.

2. **Processing Flexibility:** You should be able to enhance OR transform your event data by combining it with your other _internal_ data, e.g. stored in your transactional systems. Rudder makes that possible because it provides a powerful JS-based event transformation framework. Furthermore, since Rudder runs _inside_ your cloud or on-prem environment, you can access your production data to join with the event data.

3. **Unlimited Events:** Event volume-based pricing of most commercial systems is broken. You should be able to collect as much data as possible without worrying about overrunning event budgets. Rudder's core BE is open-source and free to use.

# Getting Started ?

1. Add these lines to your ```app/build.gradle```
```
repositories {
  maven {
    url  "https://dl.bintray.com/rudderlabs-bintray/rudder-sdk-android-core"
  }
}
```
2. Add the dependency under ```dependencies```
```
implementation 'com.rudderlabs.android.sdk:rudder-sdk-core:0.4'
```

# Initialize ```RudderClient```
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

# Send Events
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
