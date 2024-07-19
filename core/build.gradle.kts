plugins {
    id("java-library")
    id("kotlin")
}

dependencies {
    api(project(":rudderjsonadapter"))
    api(project(":web"))

    compileOnly(libs.gson)
    compileOnly(libs.jackson.core)
    compileOnly(libs.jackson.module)
    compileOnly(libs.moshi)
    compileOnly(libs.moshi.kotlin)
    compileOnly(libs.moshi.adapters)


    testImplementation(libs.awaitility)
    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockk)
    testImplementation(libs.json.assert)

    testImplementation(project(":moshirudderadapter"))
    testImplementation(project(":libs:test-common"))
    testImplementation(project(":gsonrudderadapter"))
    testImplementation(project(":jacksonrudderadapter"))
}

apply(from = "${project.projectDir.parentFile}/gradle/artifacts-jar.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/mvn-publish.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/codecov.gradle")
