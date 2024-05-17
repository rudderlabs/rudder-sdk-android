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

# These rules should be kept as part of the reporter module, as the fields of Entities are getting removed
# this deals with `There should be at least one field in @Entity`

# Required for the usage off TypeToken class in Utils.converToMap, Utils.convertToList
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Required for the serialization of SourceConfig once it is downloaded.
-keep class com.google.gson.internal.LinkedTreeMap { *; }
-keep class * implements java.io.Serializable { *; }

# Required for the usage of annotations across reporter and web modules
-dontwarn com.fasterxml.jackson.annotation.JsonIgnore
-dontwarn com.squareup.moshi.Json
-dontwarn com.fasterxml.jackson.annotation.JsonProperty

# the errors didn't go even after adding the below rule, hence needed to remove the instrumentation tests for the time being.
-dontwarn com.google.errorprone.annotations

# Required for Amplitude Device Mode
-keep class com.rudderstack.android.integrations.amplitude.AmplitudeDestinationConfig { *; }
