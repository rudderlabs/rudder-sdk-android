/*
 * Creator: Debanjan Chatterjee on 15/12/21, 3:21 PM Last modified: 12/12/21, 12:10 AM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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
package com.rudderstack.models.android

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

// import static android.content.Context.TELEPHONY_SERVICE;
//
// import android.annotation.SuppressLint;
// import android.app.Application;
// import android.bluetooth.BluetoothAdapter;
// import android.content.Context;
// import android.net.wifi.WifiManager;
// import android.provider.Settings;
// import android.telephony.TelephonyManager;
class RudderNetwork(
    @SerializedName("carrier")
    @JsonProperty("carrier")
    @Json(name = "carrier")
    private val carrier: String? = null,

    @SerializedName("wifi")
    @JsonProperty("wifi")
    @Json(name = "wifi")
    private val isWifiEnabled: Boolean = false,

    @SerializedName("bluetooth")
    @JsonProperty("bluetooth")
    @Json(name = "bluetooth")
    private val isBluetoothEnabled: Boolean = false,

    @SerializedName("cellular")
    @JsonProperty("cellular")
    @Json(name = "cellular")
    private val isCellularEnabled: Boolean = false,
) { /*@SuppressLint("MissingPermission")
    RudderNetwork(Application application) {
        try {
            // carrier name
            TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
            this.carrier = telephonyManager != null ? telephonyManager.getNetworkOperatorName() : "NA";

            // wifi enabled
            WifiManager wifi = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
            isWifiEnabled = wifi != null && wifi.isWifiEnabled();

            // bluetooth
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            isBluetoothEnabled = bluetoothAdapter != null
                    && bluetoothAdapter.isEnabled()
                    && bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON;

            // cellular status
            TelephonyManager tm = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null && tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                isCellularEnabled = Settings.Global.getInt(application.getContentResolver(), "mobile_data", 1) == 1;
            }
        } catch (Exception ex) {
            RudderLogger.logError(ex);
        }
    }*/
}
