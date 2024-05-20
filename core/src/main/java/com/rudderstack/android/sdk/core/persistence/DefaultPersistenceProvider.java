package com.rudderstack.android.sdk.core.persistence;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.ReportManager;
import com.rudderstack.android.sdk.core.RudderLogger;


import net.zetetic.database.sqlcipher.SQLiteDatabase;

import java.io.File;
import java.util.Collections;


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
    public Persistence get(Persistence.DbCreateListener dbCreateListener) {
        if (!params.isEncrypted
                || params.encryptionKey == null || params.encryptedDbName == null) {
            ReportManager.leaveBreadcrumb(ReportManager.METADATA_SECTION_PERSISTENCE, ReportManager.METADATA_PERSISTENCE_KEY_IS_ENCRYPTED,
                    true);
            return getDefaultPersistence(dbCreateListener);
        } else {
            ReportManager.incrementDbEncryptionCounter(1, Collections.singletonMap(
                    ReportManager.LABEL_TYPE, ReportManager.LABEL_TYPE_CREATED
            ));
            ReportManager.leaveBreadcrumb(ReportManager.METADATA_SECTION_PERSISTENCE, ReportManager.METADATA_PERSISTENCE_KEY_IS_ENCRYPTED,
                    false);
            return getEncryptedPersistence(dbCreateListener);
        }
    }

    @NonNull
    private EncryptedPersistence getEncryptedPersistence(@Nullable Persistence.DbCreateListener dbCreateListener) {
        //enable sqlcipher db
        initCipheredDatabase();
        File encryptedDbPath = application.getDatabasePath(params.encryptedDbName);
        if (!checkDatabaseExists(params.encryptedDbName)
                && checkDatabaseExists(params.dbName)) {
            migrateToEncryptedDatabase(encryptedDbPath);
        } else {
            if (!checkIfEncryptionIsValid(encryptedDbPath))
                deleteEncryptedDb();             //drop database
        }
        return createEncryptedObject(dbCreateListener);
    }

    @NonNull
    private EncryptedPersistence createEncryptedObject(@Nullable Persistence.DbCreateListener dbCreateListener) {
        return new EncryptedPersistence(application,
                new EncryptedPersistence.DbParams(params.encryptedDbName,
                        params.dbVersion, params.encryptionKey), dbCreateListener);
    }


    private boolean checkIfEncryptionIsValid(File encryptedDbPath) {
        try (SQLiteDatabase database = SQLiteDatabase.openDatabase(encryptedDbPath.getAbsolutePath(),
                params.encryptionKey, null, SQLiteDatabase.OPEN_READWRITE, null)) {
            Cursor cursor = database.rawQuery("PRAGMA cipher_version", null);
            cursor.close();
            return true;
        } catch (SQLiteException e) {
            ReportManager.reportError(e);
            RudderLogger.logError("Encryption key is invalid: Dumping the database and constructing a new one");
        }
        return false;
    }

    @NonNull
    private DefaultPersistence getDefaultPersistence(@Nullable Persistence.DbCreateListener dbCreateListener) {
        if (!checkDatabaseExists(params.dbName) &&
                checkDatabaseExists(params.encryptedDbName)) {
            initCipheredDatabase();
            createDefaultDatabase();
            try {
                migrateToDefaultDatabase(application.getDatabasePath(params.dbName));
            } catch (Exception e) {
                ReportManager.reportError(e);
                RudderLogger.logError("Encryption key is invalid: Dumping the database and constructing a new unencrypted one");
                deleteEncryptedDb();
            }
        }
        return new DefaultPersistence(application, new DefaultPersistence.DbParams(params.dbName, params.dbVersion), dbCreateListener);
    }

    private void createDefaultDatabase() {
        File databasePath = application.getDatabasePath(params.dbName);
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databasePath.getAbsolutePath(), "",null, null);
        database.close();
    }

    private void initCipheredDatabase() {
        System.loadLibrary("sqlcipher");
    }

    private void deleteEncryptedDb() {
        File encryptedDb = application.getDatabasePath(params.encryptedDbName);
        if (encryptedDb.exists()) {
            deleteFile(encryptedDb);
        }
    }

    private void migrateToDefaultDatabase(File databasePath) {
        ReportManager.incrementDbEncryptionCounter(1, Collections.singletonMap(
                ReportManager.LABEL_TYPE, ReportManager.LABEL_TYPE_MIGRATE_TO_DECRYPT
        ));

        File encryptedDb = application.getDatabasePath(params.encryptedDbName);
        String encryptedPath = encryptedDb.getAbsolutePath();
        SQLiteDatabase database = SQLiteDatabase.openDatabase(encryptedPath, params.encryptionKey, null, SQLiteDatabase.OPEN_READWRITE, null);
        //will throw exception if encryption key is invalid
        database.isDatabaseIntegrityOk();
        database.rawExecSQL(String.format("ATTACH DATABASE '%s' AS rl_persistence KEY ''",
                databasePath.getAbsolutePath()));
        database.rawExecSQL("select sqlcipher_export('rl_persistence')");
        database.rawExecSQL("DETACH DATABASE rl_persistence");
        database.close();
        deleteFile(encryptedDb);
    }

    private void deleteFile(File encryptedDb) {
        if (!encryptedDb.delete()) {
            RudderLogger.logError("Unable to delete database " + encryptedDb.getAbsolutePath());
        }
    }


    private void migrateToEncryptedDatabase(File encryptedDbPath) {
        ReportManager.incrementDbEncryptionCounter(1, Collections.singletonMap(
                ReportManager.LABEL_TYPE, ReportManager.LABEL_TYPE_MIGRATE_TO_ENCRYPT
        ));

        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(encryptedDbPath.getAbsolutePath(), params.encryptionKey,null, null);
        database.close();
        File decryptedDb = application.getDatabasePath(params.dbName);
        String decryptedPath = decryptedDb.getAbsolutePath();
        database = SQLiteDatabase.openDatabase(decryptedPath, "", null, SQLiteDatabase.OPEN_READWRITE, null);
        database.rawExecSQL(String.format("ATTACH DATABASE '%s' AS rl_persistence_encrypted KEY '%s'",
                encryptedDbPath.getAbsolutePath(), params.encryptionKey));
        database.rawExecSQL("select sqlcipher_export('rl_persistence_encrypted')");
        database.rawExecSQL("DETACH DATABASE rl_persistence_encrypted");
        database.close();
        deleteFile(decryptedDb);

    }


    private boolean checkDatabaseExists(@Nullable String dbName) {
        return dbName != null && application.getDatabasePath(dbName).exists();
    }


}
