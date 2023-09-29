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

# Required for the usage off TypeToken class in Utils.converToMap, Utils.convertToList
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Required for the serialization of SourceConfig once it is downloaded.
-keep class com.google.gson.internal.LinkedTreeMap { *; }
-keep class * implements java.io.Serializable { *; }
-keep class com.rudderstack.rudderjsonadapter.RudderTypeAdapter { *; }
-keep class * extends com.rudderstack.rudderjsonadapter.RudderTypeAdapter

# Required to ensure the DefaultPersistenceProviderFactory is not removed by Proguard
# and works as expected even when the customer is not using encryption feature.
-dontwarn net.sqlcipher.Cursor
-dontwarn net.sqlcipher.database.SQLiteDatabase$CursorFactory
-dontwarn net.sqlcipher.database.SQLiteDatabase
-dontwarn net.sqlcipher.database.SQLiteOpenHelper
-keep class com.rudderstack.android.sdk.core.persistence.DefaultPersistenceProviderFactory { *; }

# Required for Device Mode Transformations
-keep class com.rudderstack.android.sdk.core.TransformationResponse { *; }
-keep class com.rudderstack.android.sdk.core.TransformationResponseDeserializer { *; }

# to make sure that serialized name annotations in model classes are not removed by the Proguard full mode.
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Required for proper serialization of the custom traits and custom context
-keep class * implements com.google.gson.JsonSerializer { *; }

# to make sure that the customContextMap, custom traits are sent in the proper format
-keepclassmembers class com.rudderstack.android.sdk.core.RudderContext { java.util.Map customContextMap; }
-keepclassmembers class com.rudderstack.android.sdk.core.RudderTraits { java.util.Map extras; }

# Required for DBEncryption feature using SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.* { *; }