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
        RudderClient.getInstance().flush();
        return Result.success();
    }
}
