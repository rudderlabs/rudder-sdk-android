<p align="center">
  <a href="https://rudderstack.com/">
    <img src="https://user-images.githubusercontent.com/59817155/121357083-1c571300-c94f-11eb-8cc7-ce6df13855c9.png">
  </a>
</p>

<p align="center"><b>The Customer Data Platform for Developers</b></p>

<p align="center">
  <a href="https://search.maven.org/search?q=g:%22com.rudderstack.android.sdk%22%20AND%20a:%22core%22">
    <img src="https://img.shields.io/maven-central/v/com.rudderstack.android.sdk/core.svg?label=Maven%20Central">
    </a>
</p>

<p align="center">
  <b>
    <a href="https://rudderstack.com">Website</a>
    ·
    <a href="https://rudderstack.com/docs/stream-sources/rudderstack-sdk-integration-guides/rudderstack-android-sdk/">Documentation</a>
    ·
    <a href="https://rudderstack.com/join-rudderstack-slack-community">Community Slack</a>
  </b>
</p>

---


# RudderStack Android SDK

RudderStack's Android SDK lets you track event data from your Android applications. After integrating the SDK, you will be able to send the event data to your preferred destination/s such as Google Analytics, Amplitude, and more.

For detailed documentation on the Android SDK, click [**here**](https://rudderstack.com/docs/stream-sources/rudderstack-sdk-integration-guides/rudderstack-android-sdk).

## Get started with the Android SDK

1. Add these lines to your project level `build.gradle` file:

```groovy
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

2. Then, add the dependency under `dependencies` as shown:

```groovy

implementation 'com.rudderstack.android.sdk:core:1.x.x'
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

For more information on the different types of events supported by the Android SDK, refer to our [**docs**](https://rudderstack.com/docs/stream-sources/rudderstack-sdk-integration-guides/rudderstack-android-sdk).

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

> The method `onIntegrationReady` accepts two arguments: the integration name (e.g. "Lotame") and the callback.

[**Registering Lotame's onSync callback**](https://github.com/rudderlabs/rudder-integration-lotame-android#register-your-onsync-callback) shows one more example of registering a callback using `onIntegrationReady`.

## Do I need to add anything to my ProGuard rules?

If you are using Proguard full mode to optimize your app, add the following lines to your Android ProGuard rules:

```java
// Reporter Module
-keep class com.rudderstack.android.ruddermetricsreporterandroid.models.LabelEntity { *; }
-keep class com.rudderstack.android.ruddermetricsreporterandroid.models.MetricEntity { *; }
-keep class com.rudderstack.android.ruddermetricsreporterandroid.models.ErrorEntity { *; }

// Required for the usage off TypeToken class in Utils.converToMap, Utils.convertToList
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

// Required for the serialization of SourceConfig once it is downloaded.
-keep class com.google.gson.internal.LinkedTreeMap { *; }
-keep class * implements java.io.Serializable { *; }
-keep class com.rudderstack.rudderjsonadapter.RudderTypeAdapter { *; }
-keep class * extends com.rudderstack.rudderjsonadapter.RudderTypeAdapter

// Required to ensure the DefaultPersistenceProviderFactory is not removed by Proguard 
// and works as expected even when the customer is not using encryption feature.
-dontwarn net.sqlcipher.Cursor
-dontwarn net.sqlcipher.database.SQLiteDatabase$CursorFactory
-dontwarn net.sqlcipher.database.SQLiteDatabase
-dontwarn net.sqlcipher.database.SQLiteOpenHelper
-keep class com.rudderstack.android.sdk.core.persistence.DefaultPersistenceProviderFactory { *; }

// Required for the usage of annotations across reporter and web modules
-dontwarn com.fasterxml.jackson.annotation.JsonIgnore
-dontwarn com.squareup.moshi.Json
-dontwarn com.fasterxml.jackson.annotation.JsonProperty

// Required for Device Mode Transformations
-keep class com.rudderstack.android.sdk.core.TransformationResponse { *; }
-keep class com.rudderstack.android.sdk.core.TransformationResponseDeserializer { *; }
```

## Contribute

We would love to see you contribute to this project. Get more information on how to contribute [**here**](./CONTRIBUTING.md).

## About RudderStack

[**RudderStack**](https://rudderstack.com/) is a **customer data platform for developers**. Our tooling makes it easy to deploy pipelines that collect customer data from every app, website and SaaS platform, then activate it in your warehouse and business tools.

More information on RudderStack can be found [**here**](https://github.com/rudderlabs/rudder-server).

## Contact us

For more support on using the RudderStack Android SDK, you can [**contact us**](https://rudderstack.com/contact/) or start a conversation on our [**Slack**](https://rudderstack.com/join-rudderstack-slack-community) channel.
