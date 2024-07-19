plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {

    namespace = "com.rudderstack.android.repository"
    compileSdk = RudderstackBuildConfig.Android.TARGET_SDK

    defaultConfig {
        minSdk = RudderstackBuildConfig.Android.MIN_SDK
        targetSdk = RudderstackBuildConfig.Android.TARGET_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = RudderstackBuildConfig.Build.JAVA_VERSION
        targetCompatibility = RudderstackBuildConfig.Build.JAVA_VERSION
    }
    kotlinOptions {
        jvmTarget = RudderstackBuildConfig.Build.JVM_TARGET
        javaParameters = true
    }
    testOptions {
        unitTests {
            this.isIncludeAndroidResources = true
        }
    }
}

dependencies {

    testImplementation(libs.android.x.test.ext.junitktx)
    testImplementation(libs.android.x.testrules)
    testImplementation(libs.awaitility)
    testImplementation(libs.hamcrest)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.android.x.test.ext.junitktx)
    androidTestImplementation(libs.android.x.test.espresso)
    androidTestImplementation(libs.android.x.testrules)
}

apply(from = "${project.projectDir.parentFile}/gradle/artifacts-aar.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/mvn-publish.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/codecov.gradle")
