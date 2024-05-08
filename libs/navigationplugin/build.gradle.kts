/*
 * Creator: Debanjan Chatterjee on 20/11/23, 4:02 pm Last modified: 20/11/23, 4:02 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}
val dependencyPath = "${project.projectDir.parentFile.parent}/dependencies.gradle"
apply(from = dependencyPath)
val deps: HashMap<String, Any> by extra
val library: HashMap<String, String> by extra
val projects: HashMap<String, String> by extra

android {
    compileSdk = library["target_sdk"] as Int

    defaultConfig {
        minSdk = library["min_sdk"] as Int
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        //for code access
        buildConfigField("String", "LIBRARY_VERSION_NAME", library["version_name"] as String)
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        javaParameters = true
    }
    namespace = "com.rudderstack.android.navigationplugin"
}

dependencies {
    implementation(deps["kotlinCore"].toString())
    compileOnly(project(projects["android"].toString()))
    compileOnly(deps["navigationRuntime"].toString())
}
tasks.withType(type = org.jetbrains.kotlin.gradle.tasks.KaptGenerateStubs::class) {
    kotlinOptions.jvmTarget = "17"
}
apply(from = "${project.projectDir.parentFile.parent}/gradle/artifacts-aar.gradle")
apply(from = "${project.projectDir.parentFile.parent}/gradle/mvn-publish.gradle")
apply(from = "${project.projectDir.parentFile.parent}/gradle/codecov.gradle")
