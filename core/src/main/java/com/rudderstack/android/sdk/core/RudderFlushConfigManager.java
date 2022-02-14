package com.rudderstack.android.sdk.core;

import android.content.Context;

import com.rudderstack.android.sdk.core.util.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class RudderFlushConfigManager {

    static void saveRudderFlushConfig(Context context, RudderFlushConfig rudderFlushConfig) {
        try {
            FileOutputStream fos = context.openFileOutput("RudderFlushConfig", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(rudderFlushConfig);
            os.close();
            fos.close();
        } catch (Exception e) {
            RudderLogger.logError("RudderServerConfigManager: saveRudderFlushConfig: Exception while saving RudderServerConfig Object to File");
            e.printStackTrace();
        }
    }

    static RudderFlushConfig getRudderFlushConfig(Context context) {
        RudderFlushConfig rudderFlushConfig = null;
        try {
            if (Utils.fileExists(context, "RudderFlushConfig")) {
                FileInputStream fis = context.openFileInput("RudderFlushConfig");
                ObjectInputStream is = new ObjectInputStream(fis);
                rudderFlushConfig = (RudderFlushConfig) is.readObject();
                is.close();
                fis.close();
            }
        } catch (Exception e) {
            RudderLogger.logError("RudderServerConfigManager: getRudderFlushConfig: Failed to read RudderServerConfig Object from File");
            e.printStackTrace();
        } finally {
            return rudderFlushConfig;
        }
    }

}
