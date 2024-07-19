plugins {
    id("java-library")
    id("kotlin")
}

dependencies {

    implementation(project(":rudderjsonadapter"))

    testImplementation(libs.awaitility)
    testImplementation(libs.gson)
    testImplementation(libs.hamcrest)
    testImplementation(libs.jackson.core)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.moshi)
    testImplementation(libs.moshi.kotlin)

    testImplementation(project(":moshirudderadapter"))
    testImplementation(project(":gsonrudderadapter"))
    testImplementation(project(":jacksonrudderadapter"))
}

apply(from = "${project.projectDir.parentFile}/gradle/artifacts-jar.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/mvn-publish.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/codecov.gradle")
