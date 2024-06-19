plugins {
    id("java-library")
    id("kotlin")
}

dependencies {
    implementation(project(":rudderjsonadapter"))
    api(libs.jackson.core)
    api(libs.jackson.module)
    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)

}

apply(from = "${project.projectDir.parentFile}/gradle/artifacts-jar.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/mvn-publish.gradle")
apply(from = "${project.projectDir.parentFile}/gradle/codecov.gradle")
