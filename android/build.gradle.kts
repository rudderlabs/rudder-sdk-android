plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {

    namespace = "com.rudderstack.android"

    compileSdk = RudderstackBuildConfig.Android.COMPILE_SDK

    buildFeatures {
        buildFeatures {
            buildConfig = true
        }
    }
    defaultConfig {
        minSdk = RudderstackBuildConfig.Android.MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField(
            "String",
            "LIBRARY_VERSION_NAME",
            RudderstackBuildConfig.Version.VERSION_NAME
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
    implementation(libs.android.core.ktx)
    api(project(":core"))
    api(project(":repository"))

    compileOnly(project(":jacksonrudderadapter"))
    compileOnly(project(":gsonrudderadapter"))
    compileOnly(project(":moshirudderadapter"))
    compileOnly(project(":rudderjsonadapter"))
    compileOnly(libs.work)
    compileOnly(libs.work.multiprocess)

    testImplementation(project(":jacksonrudderadapter"))
    testImplementation(project(":gsonrudderadapter"))
    testImplementation(project(":moshirudderadapter"))
    testImplementation(project(":rudderjsonadapter"))
    testImplementation(project(":libs:test-common"))
    testImplementation(libs.android.x.test)
    testImplementation(libs.android.x.testrules)
    testImplementation(libs.android.x.test.ext.junitktx)
    testImplementation(libs.awaitility)
    testImplementation(libs.hamcrest)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent)
    testImplementation(libs.robolectric)
    testImplementation(libs.work.test)

    androidTestImplementation(libs.android.x.test.ext.junitktx)
}

apply(from = "${project.projectDir.parentFile}/gradle/artifacts-aar.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/mvn-publish.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/codecov.gradle")
