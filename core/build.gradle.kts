plugins {
    id("java-library")
    id("kotlin")
}

dependencies {
    api(project(":rudderjsonadapter"))
    api(project(":models"))
    api(project(":web"))

    testImplementation(libs.awaitility)
    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockk)

    testImplementation(project(":moshirudderadapter"))
    testImplementation(project(":libs:test-common"))
    testImplementation(project(":gsonrudderadapter"))
    testImplementation(project(":jacksonrudderadapter"))
}

apply(from = "${project.projectDir.parentFile}/gradle/artifacts-jar.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/mvn-publish.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/codecov.gradle")
