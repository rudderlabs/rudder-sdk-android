package com.rudderstack.android.ruddermetricsreporterandroid.internal;

import androidx.annotation.NonNull;


public interface StateObserver {

    /**
     * This is called whenever the notifier's state is altered, so that observers can react
     * appropriately. This is intended for internal use only.
     */
    void onStateChange(@NonNull StateEvent event);
}
