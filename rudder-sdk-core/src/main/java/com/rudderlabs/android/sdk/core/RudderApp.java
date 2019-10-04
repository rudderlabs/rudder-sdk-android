package com.rudderlabs.android.sdk.core;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import com.google.gson.annotations.SerializedName;
import com.rudderlabs.android.sdk.core.RudderLogger;

class RudderApp {
    @SerializedName("rl_build")
    private String build;
    @SerializedName("rl_name")
    private String name;
    @SerializedName("rl_namespace")
    private String nameSpace;
    @SerializedName("rl_version")
    private String version;

    // internal constructor
    // to be used while creating a cache of context
    RudderApp(Application application) {
        try {
            String packageName = application.getPackageName();
            PackageManager packageManager = application.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                this.build = Long.toString(packageInfo.getLongVersionCode());
            else this.build = Integer.toString(packageInfo.versionCode);
            this.name = packageInfo.applicationInfo.loadLabel(packageManager).toString();
            this.nameSpace = packageName;
            this.version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ex) {
            RudderLogger.logError(ex.getCause());
        }
    }
}
