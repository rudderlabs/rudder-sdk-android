package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.gson.annotations.SerializedName;

class RudderApp {
    @SerializedName("build")
    private final String build;
    @SerializedName("name")
    private final String name;
    @SerializedName("namespace")
    private final String nameSpace;
    @SerializedName("version")
    private final String version;

    // internal constructor
    // to be used while creating a cache of context
    RudderApp(Application application) {
        String buildValue = null;
        String nameValue = null;
        String nameSpaceValue = null;
        String versionValue = null;
        try {
            String packageName = application.getPackageName();
            PackageManager packageManager = application.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            buildValue = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)?
                Long.toString(packageInfo.getLongVersionCode()): Integer.toString(packageInfo.versionCode);
            nameValue = packageInfo.applicationInfo.loadLabel(packageManager).toString();
            nameSpaceValue = packageName;
            versionValue = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ex) {
            ReportManager.reportError(ex);
            RudderLogger.logError(ex.getCause());
        }
        finally {
            this.build = buildValue;
            this.name = nameValue;
            this.nameSpace = nameSpaceValue;
            this.version = versionValue;
        }
    }
}
