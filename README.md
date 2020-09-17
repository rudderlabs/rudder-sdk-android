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
  maven { url  "https://dl.bintray.com/rudderstack/rudderstack" }
}
```
2. Add the dependency under ```dependencies```
```
implementation 'com.rudderstack.android.sdk:core:1.0.5'
```

## Initialize ```RudderClient```
```
val rudderClient: RudderClient = RudderClient.getInstance(
    this,
    <WRITE_KEY>,
    RudderConfig.Builder()
        .withDataPlaneUrl(<DATA_PLANE_URL>)
        .build()
)
```
or (compatible with existing Segment code)
```
RudderClient.Builder builder = new RudderClient.Builder(this, <WRITE_KEY>);
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
or (compatible with existing Segment instrumentation code)
```
String customEvent = "some_custom_event";
String propertyKey = "some_property_key";
String propertyValue = "some_property_value";
RudderClient.with(this).track(
        customEvent,
        new RudderProperty().putValue(propertyKey, propertyValue)
);
```

For more detailed documentation check [the documentation page](https://docs.rudderstack.com/sdk-integration-guide/getting-started-with-android-sdk).

## Register your callbacks
For device mode destinations- You can register callbacks, which will be executed after the nativeSDK has been successfully initialized.Use the `onIntegrationReady` method to register your callback.The following snippet shows an example:
```
rudderClient.onIntegrationReady("Lotame") {
  var nativeSDK  = (it as LotameIntegration)
  // your custom code
}
```
The variable `it` contains the intialized nativeSDK object.
Note: The method `onIntegrationReady` accepts two arguments- the integration name(eg. "Lotame") and the callback.

[Registering Lotame's onSync callback](https://github.com/rudderlabs/rudder-integration-lotame-android#register-your-onsync-callback) shows one more example of registering a callback using `onIntegrationReady`.
## Contact Us
If you come across any issues while configuring or using RudderStack, please feel free to [contact us](https://rudderstack.com/contact/) or start a conversation on our [Discord](https://discordapp.com/invite/xNEdEGw) channel. We will be happy to help you.
