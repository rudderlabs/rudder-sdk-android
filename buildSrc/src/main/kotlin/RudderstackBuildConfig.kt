import org.gradle.api.JavaVersion

object RudderstackBuildConfig {

    object Build {
        val JAVA_VERSION = JavaVersion.VERSION_17
        val JVM_TARGET = "17"
    }

    object Android {
        val COMPILE_SDK = 34
        val MIN_SDK = 19
        val TARGET_SDK = COMPILE_SDK
    }

    object Version {
        val VERSION_NAME = "\"2.0\""
    }

    object Kotlin {
        val COMPILER_EXTENSION_VERSION = "1.4.8"
    }

    object ReleaseInfo {
        val VERSION_NAME = ""
        val GROUP_NAME = ""
    }
}
