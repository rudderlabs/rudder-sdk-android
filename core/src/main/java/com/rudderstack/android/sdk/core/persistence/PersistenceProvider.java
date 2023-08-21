package com.rudderstack.android.sdk.core.persistence;

import android.app.Application;

import java.io.Serializable;

public interface PersistenceProvider {
    Persistence get();

    /**
     * Must have a default constructor.
     */
    interface Factory {
        void setDbName(String dbName);
        void setEncryptedDbName(String encryptedDbName);
        void setDbVersion(int dbVersion);
        void setEncryptionKey(String encryptionKey);
        void setIsEncrypted(boolean isEncrypted);
        PersistenceProvider create(Application application);
    }
}
