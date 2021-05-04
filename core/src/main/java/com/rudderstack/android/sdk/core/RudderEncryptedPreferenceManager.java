package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Random;

public class RudderEncryptedPreferenceManager {

    private static final String RUDDER_ENCRYPTED_PREFS = "rl_encrypted_prefs";
    private static final String RUDDER_DB_ENCRYPTION_KEY = "rl_db_encryption_key";

    private static SharedPreferences preferences;
    private static RudderEncryptedPreferenceManager instance;

    private RudderEncryptedPreferenceManager(Application application) {
        try {
            preferences = EncryptedSharedPreferences.create(
                    application,
                    RUDDER_ENCRYPTED_PREFS,
                    new MasterKey.Builder(application.getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static RudderEncryptedPreferenceManager getInstance(Application application) {
        if (instance == null) {
            instance = new RudderEncryptedPreferenceManager(application);
        }
        return instance;
    }

    public String getDbEncryptionKey() {
        String value = preferences.getString(RUDDER_DB_ENCRYPTION_KEY, null);
        if (value == null) {
            value = getRandomEncryptionKey();
            saveDbEncryptionKey(value);
        }
        return value;
    }

    private void saveDbEncryptionKey(String key) {
        preferences.edit().putString(RUDDER_DB_ENCRYPTION_KEY, key).apply();
    }

    private String getRandomEncryptionKey() {
        int randomNumber = new Random().nextInt();
        return Integer.toHexString(randomNumber);
    }
}
