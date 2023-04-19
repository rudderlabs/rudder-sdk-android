package com.rudderstack.android.sdk.core;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.rudderstack.android.sdk.core.util.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

public class RudderFlushWorkManager {
    Context context;
    RudderConfig config;
    RudderPreferenceManager preferenceManager;
    static final String RUDDER_FLUSH_CONFIG_FILE_NAME = "RudderFlushConfig";

    RudderFlushWorkManager(Context context, RudderConfig config, RudderPreferenceManager preferenceManager) {
        this.context = context;
        this.config = config;
        this.preferenceManager = preferenceManager;
    }

    void saveRudderFlushConfig(RudderFlushConfig rudderFlushConfig) {
        try(FileOutputStream fos = context.openFileOutput(RUDDER_FLUSH_CONFIG_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos)) {
            os.writeObject(rudderFlushConfig);
        } catch (Exception e) {
            RudderLogger.logError("RudderServerConfigManager: saveRudderFlushConfig: Exception while saving RudderServerConfig Object to File");
            e.printStackTrace();
        }
    }

    static RudderFlushConfig getRudderFlushConfig(Context context) {
        RudderFlushConfig rudderFlushConfig = null;

        if (Utils.fileExists(context, RUDDER_FLUSH_CONFIG_FILE_NAME)) {
            try(FileInputStream fis = context.openFileInput(RUDDER_FLUSH_CONFIG_FILE_NAME);
                ObjectInputStream is = new ObjectInputStream(fis)) {
                rudderFlushConfig = (RudderFlushConfig) is.readObject();

            } catch (Exception e) {
                RudderLogger.logError("RudderServerConfigManager: getRudderFlushConfig: Failed to read RudderServerConfig Object from File");
                e.printStackTrace();
            }
        }
        return rudderFlushConfig;
    }

    void registerPeriodicFlushWorker() {
        if (config.isPeriodicFlushEnabled()) {
            if (!Utils.isOnClassPath("androidx.work.WorkManager")) {
                RudderLogger.logWarn("EventRepository: registerPeriodicFlushWorker: WorkManager dependency not found, please add it to your build.gradle");
                return;
            }
            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            PeriodicWorkRequest flushPendingEvents =
                    new PeriodicWorkRequest.Builder(FlushEventsWorker.class, config.getRepeatInterval(), config.getRepeatIntervalTimeUnit())
                            .addTag("Flushing Pending Events Periodically")
                            .setConstraints(constraints)
                            .build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "flushEvents",
                    ExistingPeriodicWorkPolicy.KEEP,
                    flushPendingEvents);

            String periodicWorkRequestId = flushPendingEvents.getId().toString();
            preferenceManager.savePeriodicWorkRequestId(periodicWorkRequestId);

            RudderLogger.logDebug("EventRepository: registerPeriodicFlushWorker: Registered PeriodicWorkRequest with ID " + periodicWorkRequestId);
        }
    }

    void cancelPeriodicFlushWorker() {
        if (!config.isPeriodicFlushEnabled()) {
            RudderLogger.logWarn("EventRepository: cancelPeriodicFlushWorker: Periodic Flush is Disabled, no PeriodicWorkRequest to be cancelled");
            return;
        }
        if (!Utils.isOnClassPath("androidx.work.WorkManager")) {
            RudderLogger.logWarn("EventRepository: cancelPeriodicFlushWorker: WorkManager dependency not found, please add it to your build.gradle");
            return;
        }
        String periodicWorkRequestId = preferenceManager.getPeriodicWorkRequestId();
        if (periodicWorkRequestId == null) {
            RudderLogger.logWarn("EventRepository: cancelPeriodicFlushWorker: Couldn't find PeriodicWorkRequest Id, cannot cancel PeriodicWorkRequest");
            return;
        }
        WorkManager.getInstance(context).cancelWorkById(UUID.fromString(periodicWorkRequestId));
        RudderLogger.logDebug("EventRepository: cancelPeriodicFlushWorker: Successfully cancelled PeriodicWorkRequest With ID " + periodicWorkRequestId);
    }
}
