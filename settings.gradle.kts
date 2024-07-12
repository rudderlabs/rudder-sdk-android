dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://maven.pkg,space/public/p/compose/dev") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        jcenter() // Warning: this repository is going to shut down soon
    }
}

rootProject.name = "Rudder-Android-Libs"

include(
    ":core",
    ":android",
    ":web",
    ":gsonrudderadapter",
    ":repository",
    ":rudderjsonadapter",
    ":rudderreporter",
    ":jacksonrudderadapter",
    ":moshirudderadapter",
    ":libs:navigationplugin",
    ":libs:test-common",
    ":libs:sync",
    ":samples:sample-kotlin-android",
    ":samples:kotlin-jvm-app",
)
