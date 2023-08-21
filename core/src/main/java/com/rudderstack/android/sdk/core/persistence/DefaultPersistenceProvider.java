package com.rudderstack.android.sdk.core.persistence;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;


public class DefaultPersistenceProvider implements PersistenceProvider {

    private final Application application;
    private final ProviderParams params;


    DefaultPersistenceProvider(Application application, ProviderParams params) {
        this.application = application;
        this.params = params;
    }

    static class ProviderParams {
        final String dbName;

        final @Nullable String encryptedDbName;
        final int dbVersion;
        final boolean isEncrypted;
        final String encryptionKey;


        ProviderParams(String dbName, @Nullable String encryptedDbName,
                       int dbVersion, boolean isEncrypted, String encryptionKey) {
            this.dbName = dbName;
            this.dbVersion = dbVersion;
            this.isEncrypted = isEncrypted;
            this.encryptedDbName = encryptedDbName;
            this.encryptionKey = encryptionKey;
        }
    }

    @Override
    public Persistence get() {
        if (! params.isEncrypted) {
            return getDefaultPersistence();
        } else {
            return getEncryptedPersistence();
        }
    }

    @NonNull
    private EncryptedPersistence getEncryptedPersistence() {
        //enable sqlcipher db
        initCipheredDatabase();
        if(! checkDatabaseExists(application, params.encryptedDbName)
        && checkDatabaseExists(application, params.dbName)){
            migrateToEncryptedDatabase(application.getDatabasePath(params.encryptedDbName));
        }
        return new EncryptedPersistence(application, new EncryptedPersistence.DbParams(params.encryptedDbName,
                params.dbVersion, params.encryptionKey));
    }

    @NonNull
    private DefaultPersistence getDefaultPersistence() {
        if(! checkDatabaseExists(application, params.dbName) &&
                checkDatabaseExists(application, params.encryptedDbName)){
            initCipheredDatabase();
            migrateToDefaultDatabase(application.getDatabasePath(params.dbName));
        }
        return new DefaultPersistence(application, new DefaultPersistence.DbParams(params.dbName, params.dbVersion));
    }

    private void initCipheredDatabase() {
        SQLiteDatabase.loadLibs(application);
    }


    private void migrateToDefaultDatabase(File databasePath) {
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databasePath.getAbsolutePath(), "", null);
        database.close();
        File encryptedDb = application.getDatabasePath(params.encryptedDbName);
        String encryptedPath = encryptedDb.getAbsolutePath();
        database = SQLiteDatabase.openDatabase(encryptedPath, params.encryptionKey, null, SQLiteDatabase.OPEN_READWRITE);
        database.rawExecSQL(String.format("ATTACH DATABASE '%s' AS rl_persistence KEY ''",
                databasePath.getAbsolutePath()));
        database.rawExecSQL("select sqlcipher_export('rl_persistence')");
        database.rawExecSQL("DETACH DATABASE rl_persistence");
        database.close();
        encryptedDb.delete();
    }


    private void migrateToEncryptedDatabase(File encryptedDbPath) {
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(encryptedDbPath.getAbsolutePath(), params.encryptionKey, null);
        database.close();
        File decryptedDb = application.getDatabasePath(params.dbName);
        String decryptedPath = decryptedDb.getAbsolutePath();
        database = SQLiteDatabase.openDatabase(decryptedPath, "", null, SQLiteDatabase.OPEN_READWRITE);
        database.rawExecSQL(String.format("ATTACH DATABASE '%s' AS rl_persistence_encrypted KEY '%s'",
                encryptedDbPath.getAbsolutePath(), params.encryptionKey));
        database.rawExecSQL("select sqlcipher_export('rl_persistence_encrypted')");
        database.rawExecSQL("DETACH DATABASE rl_persistence_encrypted");
        database.close();
        decryptedDb.delete();
    }

    private boolean checkDatabaseExists(Application application, String dbName) {
        return application.getDatabasePath(dbName).exists();
    }


}
