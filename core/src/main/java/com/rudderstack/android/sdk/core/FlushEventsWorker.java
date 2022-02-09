package com.rudderstack.android.sdk.core;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class FlushEventsWorker extends Worker {

    public FlushEventsWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        // Need to decide if we would like to have a retry-policy by defining back-off policy
        // Retry with backoff doesn't make more sense for Periodic work requests
        // Result.retry();
        RudderClient.getInstance().flush();
        return Result.success();
    }
}
