/*
 * Creator: Debanjan Chatterjee on 18/03/24, 11:44 am Last modified: 18/03/24, 11:44 am
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
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
    compileSdk = library["target_sdk"].toString().toInt()

    defaultConfig {
        minSdk = library["min_sdk"].toString().toInt()
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        javaParameters = true
    }
    testOptions {
        unitTests {
            this.isIncludeAndroidResources = true
        }
    }
    namespace = "com.rudderstack.android.sync"
}

dependencies {
    implementation(deps["kotlinCore"].toString())
    compileOnly(project(":core"))
    compileOnly(project(":android"))
    //dependency on work manager
    implementation(deps["work"].toString())
    implementation(deps["workMultiprocess"].toString())

    testImplementation(project(":core"))
    testImplementation(project(":android"))
    testImplementation(deps["workTest"].toString())
    testImplementation(deps["androidXTest"].toString())
    testImplementation(deps["hamcrest"].toString())
    testImplementation(deps["mockito"].toString())
    testImplementation(deps["mockito_kotlin"].toString())
    testImplementation(deps["mockk"].toString())
    testImplementation(deps["mockk_agent_jvm"].toString())
    testImplementation(deps["awaitility"].toString())
    testImplementation(deps["robolectric"].toString())
    testImplementation(deps["androidXTestExtJunitKtx"].toString())
    testImplementation(deps["androidXTestRules"].toString())
    testImplementation(deps["junit"].toString())


    androidTestImplementation(deps["androidXTestExtJunitKtx"].toString())
}
tasks.withType(type = org.jetbrains.kotlin.gradle.tasks.KaptGenerateStubs::class) {
    kotlinOptions.jvmTarget = "1.8"
}

apply(from = "${project.projectDir.parentFile.parent}/gradle/artifacts-aar.gradle")
apply(from = "${project.projectDir.parentFile.parent}/gradle/mvn-publish.gradle")
apply(from = "${project.projectDir.parentFile.parent}/gradle/codecov.gradle")
