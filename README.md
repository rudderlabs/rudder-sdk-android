[![Maven Central](https://img.shields.io/maven-central/v/com.rudderstack.android.sdk/core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.rudderstack.android.sdk%22%20AND%20a:%22core%22)

# What is RudderStack?

[RudderStack](https://rudderstack.com/) is a **customer data pipeline** tool for collecting, routing and processing data from your websites, apps, cloud tools, and data warehouse.

More information on RudderStack can be found [here](https://github.com/rudderlabs/rudder-server).

## RudderStack Android SDK

The RudderStack Android SDK allows you to track event data from your Android apps. After integrating this SDK, you will also be able to send the event data to your preferred destinations such as Google Analytics, Amplitude, and more.

## Getting Started with the RudderStack Android SDK

1. Add these lines to your project level `build.gradle` file
```
buildscript {
    repositories {
        mavenCentral()
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}
```
2. Add the dependency under ```dependencies```
```
implementation 'com.rudderstack.android.sdk:core:1.0.20'
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

For more detailed documentation check [the documentation page](https://docs.rudderstack.com/rudderstack-sdk-integration-guides/rudderstack-android-sdk).

## Register your callbacks

For device mode destinations, you can register callbacks, which will be executed after the native SDK has been successfully initialized. Use the `onIntegrationReady` method to register your callback.

The following snippet shows an example:

```
rudderClient.onIntegrationReady("Lotame") {
  var nativeSDK  = (it as LotameIntegration)
  // your custom code
}
```
The variable `it` contains the intialized nativeSDK object.

**Note**: The method `onIntegrationReady` accepts two arguments- the integration name(eg. "Lotame") and the callback.

[Registering Lotame's onSync callback](https://github.com/rudderlabs/rudder-integration-lotame-android#register-your-onsync-callback) shows one more example of registering a callback using `onIntegrationReady`.

## Contact Us

If you come across any issues while configuring or using the RudderStack Android SDK, please feel free to start a conversation on our [Slack](https://resources.rudderstack.com/join-rudderstack-slack) channel. We will be happy to help you.
