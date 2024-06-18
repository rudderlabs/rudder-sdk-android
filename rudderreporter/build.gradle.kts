plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.kotlin.android")
}

android {

    namespace = "com.rudderstack.android.ruddermetricsreporterandroid"
    compileSdk = RudderstackBuildConfig.Android.TARGET_SDK

    defaultConfig {
        minSdk =  RudderstackBuildConfig.Android.MIN_SDK
        targetSdk =  RudderstackBuildConfig.Android.TARGET_SDK
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

    implementation(libs.android.x.annotation)

    api(project(":rudderjsonadapter"))
    api(project(":repository"))
    api(project(":web"))
    api(project(":moshirudderadapter"))

    compileOnly(libs.gson)
    compileOnly(libs.jackson.core)
    compileOnly(libs.moshi)
    compileOnly(libs.moshi.kotlin)
    compileOnly(libs.moshi.adapters)

    testImplementation(project(":moshirudderadapter"))
    testImplementation(project(":gsonrudderadapter"))
    testImplementation(project(":jacksonrudderadapter"))
    testImplementation(project(":repository"))
    testImplementation(libs.android.x.test)
    testImplementation(libs.android.x.testrules)
    testImplementation(libs.android.x.test.ext.junitktx)
    testImplementation(libs.awaitility)
    testImplementation(libs.gson)
    testImplementation(libs.jackson.core)
    testImplementation(libs.hamcrest)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.moshi)
    testImplementation(libs.moshi.kotlin)
    testImplementation(libs.robolectric)

    androidTestImplementation(project(":jacksonrudderadapter"))
    androidTestImplementation(libs.android.x.test)
    androidTestImplementation(libs.android.x.testrules)
    androidTestImplementation(libs.android.x.testrunner)
    androidTestImplementation(libs.android.x.test.ext.junitktx)
    androidTestImplementation(libs.moshi)
    androidTestImplementation(libs.moshi.kotlin)
}

apply(from = "${project.projectDir.parentFile}/gradle/artifacts-aar.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/mvn-publish.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/codecov.gradle")
