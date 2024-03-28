import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
}
var sampleRudderProperties = Properties()
if (project.rootProject.file("samples/sample-kotlin-android/local.properties").canRead()) {
    sampleRudderProperties.apply {
        load(
            FileInputStream(
                File(
                    rootProject.rootDir, "samples/sample-kotlin-android/local" + ".properties"
                )
            )
        )
    }
}

android {
    val majorVersion = 0
    val minVersion = 1
    val patchVersion = 0
    val libraryVersionName = "${majorVersion}.${minVersion}.${patchVersion}"
    val libraryVersionCode = majorVersion * 1000 + minVersion * 100 + patchVersion
    val javaVersion = JavaVersion.VERSION_19
    val jvm = 19
    val composeCompilerVersion = "1.4.8"
    val androidCompileSdkVersion = 34
    val androidMinSdkVersion = 26

    compileSdk = androidCompileSdkVersion

    defaultConfig {
        applicationId = "com.rudderstack.android.sampleapp"
        minSdk = androidMinSdkVersion
        targetSdk = androidCompileSdkVersion
        versionCode = libraryVersionCode
        versionName = libraryVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String", "WRITE_KEY", sampleRudderProperties.getProperty("writeKey")
        )
        buildConfigField(
            "String", "WRITE_KEY_SECONDARY", sampleRudderProperties.getProperty("writeKeySecondary")
        )
        buildConfigField(
            "String", "CONTROL_PLANE_URL", sampleRudderProperties.getProperty("controlplaneUrl")
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
        jvmTarget = "19"
    }
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(jvm))
        }
    }
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

    buildFeatures {
        buildConfig = true
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    //compose
    implementation("androidx.compose.ui:ui:1.4.3")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:1.4.3")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:1.4.3")
    // Material Design
    implementation("androidx.compose.material:material:1.4.3")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:1.4.3")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    // Integration with activities
    implementation("androidx.activity:activity-compose:1.8.2")
    // Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    // Integration with observables
    implementation("androidx.compose.runtime:runtime-livedata:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation(project(":android"))
    implementation(project(":moshirudderadapter"))
    implementation(project(":gsonrudderadapter"))
    implementation(project(":jacksonrudderadapter"))
    implementation(project(":repository"))
    implementation(project(":core"))
    implementation(project(":models"))
    implementation(project(":web"))
    implementation(project(":rudderjsonadapter"))
    implementation(project(":rudderreporter"))


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
}
