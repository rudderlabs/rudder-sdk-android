# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Required for Web, Reporter, RudderJsonAdapter, MoshiRudderAdapter, GsonRudderAdapter, JacksonRudderAdapter Modules
-keep class com.rudderstack.rudderjsonadapter.RudderTypeAdapter { *; }
-keep class * extends com.rudderstack.rudderjsonadapter.RudderTypeAdapter

# Required for Web, Models, Reporter, JacksonRudderAdapter Modules
-dontwarn com.fasterxml.jackson.annotation.JsonProperty

# Required for Web, Reporter, MoshiRudderAdapter
-dontwarn com.squareup.moshi.Json

# Required for Reporter Module
-dontwarn com.fasterxml.jackson.annotation.JsonIgnore
-keep class com.rudderstack.android.ruddermetricsreporterandroid.models.LabelEntity { *; }
-keep class com.rudderstack.android.ruddermetricsreporterandroid.models.MetricEntity { *; }
-keep class com.rudderstack.android.ruddermetricsreporterandroid.models.ErrorEntity { *; }