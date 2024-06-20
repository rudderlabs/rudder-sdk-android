plugins {
    id("java-library")
    id("kotlin")
}

dependencies {
    compileOnly(libs.gson)
    compileOnly(libs.jackson.core)
    compileOnly(libs.jackson.module)
    compileOnly(libs.moshi)
    compileOnly(libs.moshi.kotlin)
    compileOnly(libs.moshi.adapters)

    testImplementation(libs.hamcrest)
    testImplementation(libs.junit)
    testImplementation(libs.gson)
    testImplementation(libs.jackson.core)
    testImplementation(libs.moshi)
    testImplementation(libs.moshi.kotlin)
    testImplementation(libs.moshi.adapters)
    testImplementation(libs.json.assert)

    testImplementation(project(":moshirudderadapter"))
    testImplementation(project(":gsonrudderadapter"))
    testImplementation(project(":jacksonrudderadapter"))
    testImplementation(project(":rudderjsonadapter"))
}

apply(from = "${project.projectDir.parentFile}/gradle/artifacts-jar.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/mvn-publish.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/codecov.gradle")
