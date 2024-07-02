package com.rudderstack.models.android

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class RudderApp(
    @SerializedName("build")
    @JsonProperty("build")
    @Json(name = "build")
    private val build: String,
    @SerializedName("name")
    @JsonProperty("name")
    @Json(name = "name")
    private val name: String,
    @SerializedName("namespace")
    @JsonProperty("namespace")
    @Json(name = "namespace")
    private val nameSpace: String,
    @SerializedName("version")
    @JsonProperty("version")
    @Json(name = "version")
    private val version: String,
) {

    // internal constructor
    // to be used while creating a cache of context
    /*RudderApp(Application application) {
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
    }*/
}
