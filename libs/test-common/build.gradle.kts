/*
 * Creator: Debanjan Chatterjee on 05/12/23, 12:04 pm Last modified: 05/12/23, 12:04 pm
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
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}
val dependencyPath = "${project.projectDir.parentFile.parent}/dependencies.gradle"
apply(from = dependencyPath)
val deps : HashMap<String, Any> by extra
val library : HashMap<String, String> by extra
val projects : HashMap<String, String> by extra
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
dependencies {
    compileOnly(project(projects["core"].toString()))
}