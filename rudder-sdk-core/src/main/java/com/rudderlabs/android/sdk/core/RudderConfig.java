package com.rudderlabs.android.sdk.core;

import android.text.TextUtils;
import android.webkit.URLUtil;

import java.util.ArrayList;
import java.util.List;

/*
 * Config class for RudderClient
 * - endPointUri -> API endpoint for flushing events
 * - flushQueueSize -> maximum number of events to be batched
 * - dbCountThreshold -> maximum number of events to be persisted in local DB
 * - sleepTimeOut -> timeout for automatic flushing since last successful flush
 * - logLevel -> level of logging for debugging
 *
 * default values are set at Constants file
 *
 * */
public class RudderConfig {
    private String endPointUri;
    private int flushQueueSize;
    private int dbCountThreshold;
    private int sleepTimeOut;
    private int logLevel;
    private List<RudderIntegration.Factory> factories;

    // internal constructor for creating instance with default values
    RudderConfig() throws RudderException {
        this(Constants.BASE_URL, Constants.FLUSH_QUEUE_SIZE, Constants.DB_COUNT_THRESHOLD, Constants.SLEEP_TIMEOUT, RudderLogger.RudderLogLevel.ERROR, null);
    }

    // internal constructor to be used along with RudderConfigBuilder
    RudderConfig(String endPointUri, int flushQueueSize, int dbCountThreshold, int sleepTimeOut, int logLevel, List<RudderIntegration.Factory> factories) throws RudderException {
        RudderLogger.init(logLevel);
        if (TextUtils.isEmpty(endPointUri)) {
            throw new RudderException("endPointUri can not be null or empty");
        }
        if (!URLUtil.isValidUrl(endPointUri)) {
            throw new RudderException("malformed endPointUri");
        }
        // check if endpoint uri is formatted correctly
        if (!endPointUri.endsWith("/")) endPointUri += "/";
        this.endPointUri = endPointUri;
        if (flushQueueSize < 1 || flushQueueSize > 100) {
            throw new RudderException("flushQueueSize is out of range. Min: 1, Max: 100");
        }
        this.flushQueueSize = flushQueueSize;
        this.logLevel = logLevel;
        this.dbCountThreshold = dbCountThreshold;
        this.sleepTimeOut = sleepTimeOut;
        this.factories = factories;
    }

    /*
    * getters
    * */
    public String getEndPointUri() {
        return endPointUri;
    }

    public int getFlushQueueSize() {
        return flushQueueSize;
    }

    public int getDbCountThreshold() {
        return dbCountThreshold;
    }

    public int getSleepTimeOut() {
        return sleepTimeOut;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public List<RudderIntegration.Factory> getFactories() {
        return factories;
    }
}
