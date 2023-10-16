package com.rudderstack.android.sdk.core;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.Data;
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
    static final String PERSISTENCE_PROVIDER_FACTORY_CLASS_NAME_KEY = "persistenceProviderFactory";

    private static final String UNIQUE_FLUSH_WORK_NAME = "flushEvents";
    RudderFlushWorkManager(Context context, RudderConfig config, RudderPreferenceManager preferenceManager) {
        this.context = context;
        this.config = config;
        this.preferenceManager = preferenceManager;
    }

    void saveRudderFlushConfig(RudderFlushConfig rudderFlushConfig) {
        try (FileOutputStream fos = context.openFileOutput(RUDDER_FLUSH_CONFIG_FILE_NAME, Context.MODE_PRIVATE);
             ObjectOutputStream os = new ObjectOutputStream(fos)) {
            os.writeObject(rudderFlushConfig);
        } catch (Exception e) {
            ReportManager.reportError(e);
            RudderLogger.logError("RudderServerConfigManager: saveRudderFlushConfig: Exception while saving RudderServerConfig Object to File");
            e.printStackTrace();
        }
    }

    static RudderFlushConfig getRudderFlushConfig(Context context) {
        RudderFlushConfig rudderFlushConfig = null;

        if (Utils.fileExists(context, RUDDER_FLUSH_CONFIG_FILE_NAME)) {
            try (FileInputStream fis = context.openFileInput(RUDDER_FLUSH_CONFIG_FILE_NAME);
                 ObjectInputStream is = new ObjectInputStream(fis)) {
                rudderFlushConfig = (RudderFlushConfig) is.readObject();

            } catch (Exception e) {
                ReportManager.reportError(e);
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
            String persistenceProviderFactory = config.getDbEncryption().getPersistenceProviderFactoryClassName();
            if (persistenceProviderFactory == null)
                persistenceProviderFactory = "";
            PeriodicWorkRequest flushPendingEvents =
                    new PeriodicWorkRequest.Builder(FlushEventsWorker.class, config.getRepeatInterval(), config.getRepeatIntervalTimeUnit())
                            .addTag("Flushing Pending Events Periodically")
                            .setConstraints(constraints)
                            .setInputData(new Data.Builder().putString(PERSISTENCE_PROVIDER_FACTORY_CLASS_NAME_KEY,
                                    persistenceProviderFactory).build())
                            .build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    UNIQUE_FLUSH_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    flushPendingEvents);

            RudderLogger.logDebug("EventRepository: registerPeriodicFlushWorker: Registered PeriodicWorkRequest with name " + UNIQUE_FLUSH_WORK_NAME);
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
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_FLUSH_WORK_NAME);
        RudderLogger.logDebug("EventRepository: cancelPeriodicFlushWorker: Successfully cancelled PeriodicWorkRequest With name " + UNIQUE_FLUSH_WORK_NAME);
    }
}
