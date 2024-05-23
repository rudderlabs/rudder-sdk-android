// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra["compose_version"] = RudderstackBuildConfig.Kotlin.COMPILER_EXTENSION_VERSION
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

plugins {
    alias(libs.plugins.gradle.nexus.publish)
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

subprojects {
    version = properties[RudderstackBuildConfig.ReleaseInfo.VERSION_NAME].toString()
    group = properties[RudderstackBuildConfig.ReleaseInfo.GROUP_NAME].toString()
}

apply(from = rootProject.file("gradle/promote.gradle"))
apply(from = rootProject.file("gradle/codecov.gradle"))
