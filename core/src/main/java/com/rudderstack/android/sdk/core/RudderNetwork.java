package com.rudderstack.android.sdk.core;

import static android.Manifest.permission.BLUETOOTH;
import static android.content.Context.TELEPHONY_SERVICE;
import static com.rudderstack.android.sdk.core.util.Utils.isTv;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.gson.annotations.SerializedName;

class RudderNetwork {
    @SerializedName("carrier")
    private String carrier;
    @SerializedName("wifi")
    private boolean isWifiEnabled = false;
    @SerializedName("bluetooth")
    private Boolean isBluetoothEnabled;
    @SerializedName("cellular")
    private boolean isCellularEnabled = false;

    @SuppressLint("MissingPermission")
    RudderNetwork(Application application) {
        try {
            // carrier name
            if (!isTv(application)) {
                TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
                this.carrier = telephonyManager != null ? telephonyManager.getNetworkOperatorName() : "NA";
            }

            // wifi enabled
            WifiManager wifi = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
            isWifiEnabled = wifi != null && wifi.isWifiEnabled();

            // bluetooth
            try {
                Context context = application.getApplicationContext();
                if (context.checkCallingOrSelfPermission(BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    isBluetoothEnabled = bluetoothAdapter != null
                            && bluetoothAdapter.isEnabled()
                            && bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON;
                } else {
                    RudderLogger.logWarn("RudderNetwork: Cannot check bluetooth status as permission is absent");
                }
            } catch (Exception e) {
                RudderLogger.logError("RudderNetwork: Exception during bluetooth permission check");
            }


            // cellular status
            TelephonyManager tm = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null && tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    isCellularEnabled = Settings.Global.getInt(application.getContentResolver(), "mobile_data", 1) == 1;
                } else {
                    isCellularEnabled = Settings.Secure.getInt(application.getContentResolver(), "mobile_data", 1) == 1;
                }
            }
        } catch (Exception ex) {
            RudderLogger.logError(ex);
        }
    }
}
