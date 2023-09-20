package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

class AppVersion {

    int previousBuild;
    int currentBuild;
    String previousVersion;
    String currentVersion;

    RudderPreferenceManager preferenceManager;

    AppVersion(Application application) {
        try {
            preferenceManager = RudderPreferenceManager.getInstance(application);
            previousBuild = preferenceManager.getBuildNumber();
            previousVersion = preferenceManager.getVersionName();
            RudderLogger.logDebug("Previous Installed Version: " + previousVersion);
            RudderLogger.logDebug("Previous Installed Build: " + previousBuild);
            String packageName = application.getPackageName();
            PackageManager packageManager = application.getPackageManager();
            if (packageManager == null)
                return;
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            currentVersion = packageInfo.versionName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                currentBuild = (int) packageInfo.getLongVersionCode();
            } else {
                currentBuild = packageInfo.versionCode;
            }
            RudderLogger.logDebug("Current Installed Version: " + currentVersion);
            RudderLogger.logDebug("Current Installed Build: " + currentBuild);
        } catch (PackageManager.NameNotFoundException ex) {
            ReportManager.reportError(ex);
            RudderLogger.logError(ex);
        }
    }

    /*
     * Call this method to store the Current Build and Current Version of the app.
     * In case of the LifeCycle events Application Installed or Application Updated only.
     */
    void storeCurrentBuildAndVersion() {
        preferenceManager.saveBuildNumber(currentBuild);
        preferenceManager.saveVersionName(currentVersion);
    }

    boolean isApplicationInstalled() {
        return this.previousBuild == -1;
    }

    boolean isApplicationUpdated() {
        return this.previousBuild != -1 && (this.previousBuild != this.currentBuild);
    }
}