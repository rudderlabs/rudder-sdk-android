package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlushEventsWorker extends Worker {

    public FlushEventsWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        RudderFlushConfig flushConfig = RudderFlushWorkManager.getRudderFlushConfig(getApplicationContext());
        if (flushConfig == null) {
            RudderLogger.logWarn("FlushEventsWorker: doWork: RudderFlushConfig is empty, couldn't flush the events, aborting the work");
            return Result.failure();
        }
        RudderLogger.init(flushConfig.getLogLevel());

        DBPersistentManager dbManager = DBPersistentManager.getInstance((Application) getApplicationContext());
        RudderNetworkManager networkManager = new RudderNetworkManager(flushConfig.getAuthHeaderString(), flushConfig.getAnonymousHeaderString());
        if (dbManager == null) {
            RudderLogger.logWarn("FlushEventsWorker: doWork: Failed to initialize DBPersistentManager, couldn't flush the events, aborting the work");
            return Result.failure();
        }
        RudderLogger.logInfo("FlushEventsWorker: doWork: Started Periodic Flushing of Events ");

        return FlushUtils.flush(
                flushConfig.flushQueueSize,
                flushConfig.dataPlaneUrl,
                dbManager,
                networkManager
        ) ? Result.success() : Result.failure();
    }
}
