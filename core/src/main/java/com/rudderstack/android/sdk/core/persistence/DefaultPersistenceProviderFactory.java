package com.rudderstack.android.sdk.core.persistence;

import android.app.Application;

import com.rudderstack.android.sdk.core.RudderLogger;

public class DefaultPersistenceProviderFactory implements PersistenceProvider.Factory {
    private String dbName = null;
    private String encryptedDbName = null;
    private int dbVersion = 1;
    private String encryptionKey = null;
    private boolean isEncrypted = false;

    @Override
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public void setEncryptedDbName(String encryptedDbName) {
        this.encryptedDbName = encryptedDbName;
    }

    @Override
    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }

    @Override
    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    @Override
    public void setIsEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    @Override
    public PersistenceProvider create(Application application) {
        if (dbName == null) {
            RudderLogger.logError("DBPersistentManager: dbName is null. Aborting Db creation");
            return null;
        }
        if (dbVersion == 0) {
            RudderLogger.logWarn("DBPersistentManager: dbVersion cannot be 0. Resetting to 1");
            dbVersion = 1;
        }
        if (isEncrypted) {
            if (encryptionKey == null) {
                RudderLogger.logWarn("DBPersistentManager: isEncrypted is true but encryptionKey is null. Proceeding with null key");
            }
            if (encryptedDbName == null) {
                RudderLogger.logError("DBPersistentManager: isEncrypted is true but encryptedDbName is null. Aborting Db creation");
                return null;
            }

        }
        return new DefaultPersistenceProvider(application,
                new DefaultPersistenceProvider.ProviderParams(dbName, encryptedDbName,
                        dbVersion, isEncrypted, encryptionKey));

    }
}
