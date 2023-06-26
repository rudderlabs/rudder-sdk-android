/*
 * Creator: Debanjan Chatterjee on 09/06/23, 5:30 pm Last modified: 05/06/23, 5:52 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.internal.error

import androidx.annotation.VisibleForTesting
import com.rudderstack.android.ruddermetricsreporterandroid.Logger
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DeviceBuildInfo
import java.io.File
import java.io.IOException
import java.io.Reader

/**
 * Attempts to detect whether the device is rooted. Root detection errs on the side of false
 * negatives rather than false positives.
 *
 * This class will only give a reasonable indication that a device has been rooted - as it's
 * possible to manipulate Java return values & native library loading, it will always be possible
 * for a determined application to defeat these root checks.
 */
internal class RootDetector @JvmOverloads constructor(
    private val deviceBuildInfo: DeviceBuildInfo = DeviceBuildInfo.defaultInfo(),
    private val rootBinaryLocations: List<String> = ROOT_INDICATORS,
    private val buildProps: File = BUILD_PROP_FILE,
    private val logger: Logger
) {

    companion object {
        private val BUILD_PROP_FILE = File("/system/build.prop")

        private val ROOT_INDICATORS = listOf(
            // Common binaries
            "/system/xbin/su",
            "/system/bin/su",
            // < Android 5.0
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            // >= Android 5.0
            "/system/app/Superuser",
            "/system/app/SuperSU",
            // Fallback
            "/system/xbin/daemonsu",
            // Systemless root
            "/su/bin"
        )
    }

    @Volatile
    private var libraryLoaded = false

    init {
        try {
            System.loadLibrary("bugsnag-root-detection")
            libraryLoaded = true
        } catch (ignored: UnsatisfiedLinkError) {
            // library couldn't load. This could be due to root detection countermeasures,
            // or down to genuine OS level bugs with library loading - in either case
            // Bugsnag will default to skipping the checks.
        }
    }

    /**
     * Determines whether the device is rooted or not.
     */
    fun isRooted(): Boolean {
        return try {
            checkBuildTags() || checkSuExists() || checkBuildProps() || checkRootBinaries() || nativeCheckRoot()
        } catch (exc: Throwable) {
            logger.w("Root detection failed", exc)
            false
        }
    }

    /**
     * Checks whether the su binary exists by running `which su`. A non-empty result
     * indicates that the binary is present, which is a good indicator that the device
     * may have been rooted.
     */
    private fun checkSuExists(): Boolean = checkSuExists(ProcessBuilder())

    /**
     * Checks whether the build tags contain 'test-keys', which indicates that the OS was signed
     * with non-standard keys.
     */
    internal fun checkBuildTags(): Boolean = deviceBuildInfo.tags?.contains("test-keys") == true

    /**
     * Checks whether common root binaries exist on disk, which are a good indicator of whether
     * the device is rooted.
     */
    internal fun checkRootBinaries(): Boolean {
        runCatching {
            for (candidate in rootBinaryLocations) {
                if (File(candidate).exists()) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Checks the contents of /system/build.prop to see whether it contains dangerous properties.
     * These properties give a good indication that a phone might be using a custom
     * ROM and is therefore rooted.
     */
    internal fun checkBuildProps(): Boolean {
        runCatching {
            return buildProps.bufferedReader().useLines { lines ->
                lines
                    .map { line ->
                        line.replace("\\s".toRegex(), "")
                    }.filter { line ->
                        line.startsWith("ro.debuggable=[1]") || line.startsWith("ro.secure=[0]")
                    }.any()
            }
        }
        return false
    }

    @VisibleForTesting
    internal fun checkSuExists(processBuilder: ProcessBuilder): Boolean {
        processBuilder.command(listOf("which", "su"))

        var process: Process? = null
        return try {
            process = processBuilder.start()
            process.inputStream.bufferedReader().use { it.isNotBlank() }
        } catch (ignored: IOException) {
            false
        } finally {
            process?.destroy()
        }
    }

    private external fun performNativeRootChecks(): Boolean

    private fun Reader.isNotBlank(): Boolean {
        while (true) {
            val ch = read()
            when {
                ch == -1 -> return false
                !ch.toChar().isWhitespace() -> return true
            }
        }
    }

    /**
     * Performs root checks which require native code.
     */
    private fun nativeCheckRoot(): Boolean = when {
        libraryLoaded -> performNativeRootChecks()
        else -> false
    }
}
