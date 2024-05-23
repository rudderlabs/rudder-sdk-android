import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
}

val sampleRudderPropertiesFile: File = rootProject.file("${projectDir}/rudderstack.properties")
val sampleRudderProperties = Properties().apply {
    sampleRudderPropertiesFile.canRead().apply { load(FileInputStream(sampleRudderPropertiesFile)) }
}

android {
    val javaVersion = RudderstackBuildConfig.Build.JAVA_VERSION
    val jvm = 17
    val composeCompilerVersion = RudderstackBuildConfig.Kotlin.COMPILER_EXTENSION_VERSION
    val androidCompileSdkVersion = RudderstackBuildConfig.Android.COMPILE_SDK
    val androidMinSdkVersion = 26

    compileSdk = androidCompileSdkVersion

    defaultConfig {
        applicationId = "com.rudderstack.android.sampleapp"
        minSdk = androidMinSdkVersion
        targetSdk = androidCompileSdkVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String", "WRITE_KEY",
            sampleRudderProperties.getProperty("writeKey")
        )
        buildConfigField(
            "String", "WRITE_KEY_SECONDARY",
            sampleRudderProperties.getProperty("writeKeySecondary")
        )
        buildConfigField(
            "String", "CONTROL_PLANE_URL",
            sampleRudderProperties.getProperty("controlplaneUrl")
        )
        buildConfigField(
            "String", "CONTROL_PLANE_URL_SECONDARY",
            sampleRudderProperties.getProperty("controlplaneUrlSecondary")
        )
        buildConfigField(
            "String", "DATA_PLANE_URL",
            sampleRudderProperties.getProperty("dataplaneUrl")
        )
        buildConfigField(
            "String", "DATA_PLANE_URL_SECONDARY",
            sampleRudderProperties.getProperty("dataplaneUrlSecondary")
        )
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
                )
            )
        }
    }
    buildTypes {
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
                )
            )
        }
    }
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = RudderstackBuildConfig.Build.JVM_TARGET
        javaParameters = true
    }
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(jvm))
        }
    }
    buildFeatures {
        buildFeatures {
            compose = true
        }
        composeOptions {
            kotlinCompilerExtensionVersion = composeCompilerVersion
        }
        tasks.withType<Test> {
            useJUnitPlatform()
        }
        namespace = "com.rudderstack.android.sampleapp"
    }

    dependencies {
        implementation("com.google.android.material:material:1.12.0")
        //compose
        implementation("androidx.compose.ui:ui:1.6.7")
        implementation("androidx.compose.ui:ui-tooling-preview:1.6.7")
        implementation("androidx.compose.ui:ui-tooling:1.6.7")
        implementation("androidx.compose.foundation:foundation:1.6.7")
        // Material Design
        implementation("androidx.compose.material:material:1.6.7")
        // Integration with activities
        implementation("androidx.activity:activity-compose:1.9.0")
        // Integration with ViewModels
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

        implementation(project(":android"))
        implementation(project(":moshirudderadapter"))
        implementation(project(":gsonrudderadapter"))
        implementation(project(":jacksonrudderadapter"))
        implementation(project(":repository"))
        implementation(project(":core"))
        implementation(project(":models"))
        implementation(project(":web"))
        implementation(project(":rudderreporter"))
        implementation(project(":libs:sync"))
        implementation(project(":libs:navigationplugin"))
    }
}
