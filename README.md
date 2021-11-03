[![Maven Central](https://img.shields.io/maven-central/v/com.rudderstack.android.sdk/core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.rudderstack.android.sdk%22%20AND%20a:%22core%22)

# RudderStack Android SDK

RudderStack's Android SDK lets you track event data from your Android app. After integrating the SDK, you will be able to send the event data to your preferred destination/s such as Google Analytics, Amplitude, and more.

For detailed documentation on the Android SDK, click [**here**](https://docs.rudderstack.com/stream-sources/rudderstack-sdk-integration-guides/rudderstack-android-sdk).

## About RudderStack

[**RudderStack**](https://rudderstack.com/) is a **customer data platform for developers**. Our tooling makes it easy to deploy pipelines that collect customer data from every app, website and SaaS platform, then activate it in your warehouse and business tools.

More information on RudderStack can be found [**here**](https://github.com/rudderlabs/rudder-server).

## Getting started with the Android SDK

1. Add these lines to your project level `build.gradle` file:

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

2. Then, add the dependency under ```dependencies``` as shown:

```

implementation 'com.rudderstack.android.sdk:core:1.0.22'
```

## Initializing ```RudderClient```

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

## Sending events
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

For more detailed documentation, click [**here**](https://docs.rudderstack.com/stream-sources/rudderstack-sdk-integration-guides/rudderstack-android-sdk).

## Registering your callbacks

For device mode destinations, you can register callbacks, which will be executed after the native SDK has been successfully initialized. Use the `onIntegrationReady` method to register your callback.

The following snippet shows an example:

```
rudderClient.onIntegrationReady("Lotame") {
  var nativeSDK  = (it as LotameIntegration)
  // your custom code
}
```
The variable `it` contains the intialized nativeSDK object.

> The method `onIntegrationReady` accepts two arguments- the integration name(eg. "Lotame") and the callback.
---

[**Registering Lotame's onSync callback**](https://github.com/rudderlabs/rudder-integration-lotame-android#register-your-onsync-callback) shows one more example of registering a callback using `onIntegrationReady`.

## Contact us

For more support on using the RudderStack Android SDK, you can [**contact us**](https://rudderstack.com/contact/) or start a conversation on our [**Slack**](https://rudderstack.com/join-rudderstack-slack-community) channel.
