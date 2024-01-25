package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.rudderstack.android.sdk.core.gson.RudderGson;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static com.rudderstack.android.sdk.core.util.Utils.isOnClassPath;

public class RudderContext {
    @SerializedName("app")
    private RudderApp app;
    @SerializedName("traits")
    private Map<String, Object> traits;
    @SerializedName("library")
    private RudderLibraryInfo libraryInfo;
    @SerializedName("os")
    private RudderOSInfo osInfo;
    @SerializedName("screen")
    private RudderScreenInfo screenInfo;
    @SerializedName("userAgent")
    private String userAgent;
    @SerializedName("locale")
    private String locale;
    @SerializedName("device")
    private RudderDeviceInfo deviceInfo;
    @SerializedName("network")
    private RudderNetwork networkInfo;
    @SerializedName("timezone")
    private String timezone;
    @Nullable
    @SerializedName("sessionId")
    private Long sessionId = null;
    @Nullable
    @SerializedName("sessionStart")
    private Boolean sessionStart = null;
    @Nullable
    @SerializedName("consentManagement")
    private ConsentManagement consentManagement = null;
    @SerializedName("externalId")
    private List<Map<String, Object>> externalIds = null;
    public Map<String, Object> customContextMap = null;

    private static transient String _anonymousId;

    RudderContext() {
        // stop instantiating without application instance.
        // cachedContext is used every time, once initialized
    }

    RudderContext(Application application, String anonymousId, String advertisingId, String deviceToken, boolean collectDeviceId) {
        RudderPreferenceManager preferenceManger = RudderPreferenceManager.getInstance(application);

        if (TextUtils.isEmpty(anonymousId)) {
            // starting from version 1.18.0, we are completely removing the link between deviceId and anonymousId for compliance reasons
            // and from here on, UUID will be used as anonymousId
            anonymousId = preferenceManger.getCurrentAnonymousIdValue();
            if (anonymousId == null) {
                RudderLogger.logDebug("RudderContext: constructor: anonymousId is null, generating new anonymousId");
                anonymousId = UUID.randomUUID().toString();
            }
        }
        preferenceManger.saveAnonymousId(anonymousId);

        _anonymousId = anonymousId;

        this.app = new RudderApp(application);

        // get saved traits from prefs. if not present create new one and save
        String traitsJson = preferenceManger.getTraits();
        RudderLogger.logDebug(String.format(Locale.US, "Traits from persistence storage%s", traitsJson));
        if (traitsJson == null) {
            RudderTraits traits = new RudderTraits(anonymousId);
            this.traits = Utils.convertToMap(traits);
            this.persistTraits();
            RudderLogger.logDebug("New traits has been saved");
        } else {
            this.traits = Utils.convertToMap(traitsJson);
            this.traits.put("anonymousId", anonymousId);
            this.persistTraits();
            RudderLogger.logDebug("Using old traits from persistence");
        }

        // get saved external Ids from prefs. if not present set it to null
        String externalIdsJson = preferenceManger.getExternalIds();
        RudderLogger.logDebug(String.format(Locale.US, "ExternalIds from persistence storage%s", externalIdsJson));
        if (externalIdsJson != null) {
            this.externalIds = Utils.convertToList(externalIdsJson);
            RudderLogger.logDebug("Using old externalIds from persistence");
        }

        this.screenInfo = new RudderScreenInfo(application);
        this.userAgent = System.getProperty("http.agent");
        this.deviceInfo = new RudderDeviceInfo(advertisingId, deviceToken, collectDeviceId);
        this.networkInfo = new RudderNetwork(application);
        this.osInfo = new RudderOSInfo();
        this.libraryInfo = new RudderLibraryInfo();
        this.locale = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        this.timezone = Utils.getTimeZone();
    }


    void resetTraits() {
        RudderTraits traits = new RudderTraits();
        // convert the whole traits to map and take care of the extras
        this.traits = Utils.convertToMap(traits);
    }

    void updateTraits(RudderTraits traits) {
        // if traits is null reset the traits to a new one with only anonymousId
        if (traits == null) {
            traits = new RudderTraits();
        }

        // convert the whole traits to map and take care of the extras
        Map<String, Object> traitsMap = Utils.convertToMap(traits);

        String existingId = (String) this.traits.get("id");
        String newId = (String) traitsMap.get("id");

        // If a user is already loggedIn and then a new user tries to login
        if (existingId != null && newId != null && !existingId.equals(newId)) {
            this.traits = traitsMap;
            resetExternalIds();
            return;
        }

        // update traits object here
        this.traits.putAll(traitsMap);

    }

    void updateAnonymousIdTraits() {
        this.traits.put("anonymousId", _anonymousId);
    }

    void persistTraits() {
        // persist updated traits to sharedPreference
        try {
            if (RudderClient.getApplication() != null) {
                RudderPreferenceManager preferenceManger = RudderPreferenceManager.getInstance(RudderClient.getApplication());
                preferenceManger.saveTraits(RudderGson.serialize(this.traits));
            }
        } catch (NullPointerException ex) {
            ReportManager.reportError(ex);
            RudderLogger.logError(ex);
        }
    }

    public Map<String, Object> getTraits() {
        return traits;
    }

    void updateTraitsMap(Map<String, Object> traits) {
        this.traits = traits;
    }

    String getDeviceId() {
        return deviceInfo.getDeviceId();
    }

    // set the push token as passed by the developer
    void putDeviceToken(String token) {
        if (token != null && !token.isEmpty()) {
            this.deviceInfo.setToken(token);
        }
    }

    // set the values provided by the user
    void updateWithAdvertisingId(String advertisingId) {
        if (advertisingId == null || advertisingId.isEmpty()) {
            this.deviceInfo.setAdTrackingEnabled(false);
        } else {
            this.deviceInfo.setAdTrackingEnabled(true);
            this.deviceInfo.setAdvertisingId(advertisingId);
        }
    }

    void updateDeviceWithAdId() {
        if (isOnClassPath("com.google.android.gms.ads.identifier.AdvertisingIdClient")) {
            // This needs to be done each time since the settings may have been updated.
            new Thread(() -> {
                try {
                    boolean available = getGooglePlayServicesAdvertisingID();
                    if (!available) {
                        available = getAmazonFireAdvertisingID();
                    }
                    if (!available) {
                        RudderLogger.logDebug("Unable to collect advertising ID from Amazon Fire OS and Google Play Services.");
                    }
                } catch (Exception e) {
                    ReportManager.reportError(e);
                    RudderLogger.logError("Unable to collect advertising ID from Google Play Services or Amazon Fire OS.");
                }
            }).start();
        } else {
            RudderLogger.logDebug(
                    "Not collecting advertising ID because "
                            + "com.google.android.gms.ads.identifier.AdvertisingIdClient "
                            + "was not found on the classpath.");
        }
    }

    private boolean getGooglePlayServicesAdvertisingID() throws Exception {
        if (RudderClient.getApplication() == null) {
            return false;
        }

        Object advertisingInfo = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient")
                .getMethod("getAdvertisingIdInfo", Context.class).invoke(null, RudderClient.getApplication());

        if (advertisingInfo == null) {
            return false;
        }

        Boolean isLimitAdTrackingEnabled = (Boolean) advertisingInfo.getClass()
                .getMethod("isLimitAdTrackingEnabled").invoke(advertisingInfo);

        if (isLimitAdTrackingEnabled == null || isLimitAdTrackingEnabled) {
            RudderLogger.logDebug("Not collecting advertising ID because isLimitAdTrackingEnabled (Google Play Services) is true.");
            this.deviceInfo.setAdTrackingEnabled(false);
            return false;
        }

        if (TextUtils.isEmpty(this.deviceInfo.getAdvertisingId())) {
            // set the values if and only if the values are not set
            // if value exists, it must have been set by the developer. don't overwrite
            this.deviceInfo.setAdvertisingId((String) advertisingInfo.getClass().getMethod("getId").invoke(advertisingInfo));
            this.deviceInfo.setAdTrackingEnabled(true);
        }

        return true;
    }

    private boolean getAmazonFireAdvertisingID() throws Exception {
        if (RudderClient.getApplication() == null) {
            return false;
        }

        ContentResolver contentResolver = RudderClient.getApplication().getContentResolver();

        boolean limitAdTracking = Settings.Secure.getInt(contentResolver, "limit_ad_tracking") != 0;

        if (limitAdTracking) {
            RudderLogger.logDebug("Not collecting advertising ID because limit_ad_tracking (Amazon Fire OS) is true.");
            this.deviceInfo.setAdTrackingEnabled(false);
            return false;
        }

        if (TextUtils.isEmpty(this.deviceInfo.getAdvertisingId())) {
            // set the values if and only if the values are not set
            // if value exists, it must have been set by the developer. don't overwrite
            this.deviceInfo.setAdvertisingId(Settings.Secure.getString(contentResolver, "advertising_id"));
            this.deviceInfo.setAdTrackingEnabled(true);
        }

        return true;
    }

    /**
     * Getter method for Advertising ID
     *
     * @return The Advertising ID if available, returns null otherwise.
     */
    @Nullable
    public String getAdvertisingId() {
        if (this.deviceInfo == null) {
            return null;
        }
        return this.deviceInfo.getAdvertisingId();
    }

    /**
     * Getter method for Ad Tracking Status.
     *
     * @return true or false, depending on whether ad tracking is enabled or disabled.
     */
    public boolean isAdTrackingEnabled() {
        if (this.deviceInfo == null) {
            return false;
        }
        return this.deviceInfo.isAdTrackingEnabled();
    }

    /**
     * @return ExternalIds for the current session
     */
    @Nullable
    public List<Map<String, Object>> getExternalIds() {
        return externalIds;
    }

    void updateExternalIds(@NonNull List<Map<String, Object>> externalIds) {
        // update local variable
        if (this.externalIds == null) {
            this.externalIds = new ArrayList<>();
            this.externalIds.addAll(externalIds);
            return;
        }
        for (Map<String, Object> newExternalId : externalIds) {
            String newExternalIdType = (String) newExternalId.get("type");
            boolean typeAlreadyExists = false;
            if (newExternalIdType != null) {
                for (Map<String, Object> existingExternalId : this.externalIds) {
                    String existingExternalIdType = (String) existingExternalId.get("type");
                    if (existingExternalIdType != null && existingExternalIdType.equals(newExternalIdType)) {
                        typeAlreadyExists = true;
                        existingExternalId.put("id", newExternalId.get("id"));
                    }
                }
                if (!typeAlreadyExists) {
                    this.externalIds.add(newExternalId);
                }
            }
        }
    }

    void persistExternalIds() {
        // persist updated externalIds to shared preferences
        try {
            if (RudderClient.getApplication() != null) {
                RudderPreferenceManager preferenceManger = RudderPreferenceManager.getInstance(RudderClient.getApplication());
                preferenceManger.saveExternalIds(RudderGson.serialize(this.externalIds));
            }
        } catch (NullPointerException ex) {
            ReportManager.reportError(ex);
            RudderLogger.logError(ex);
        }
    }

    void resetExternalIds() {
        this.externalIds = null;
        // reset externalIds from shared preferences
        try {
            if (RudderClient.getApplication() != null) {
                RudderPreferenceManager preferenceManger = RudderPreferenceManager.getInstance(RudderClient.getApplication());
                preferenceManger.clearExternalIds();
            }
        } catch (NullPointerException ex) {
            ReportManager.reportError(ex);
            RudderLogger.logError(ex);
        }
    }

    void setCustomContexts(Map<String, Object> customContexts) {
        if (customContexts == null)
            return;
        if (this.customContextMap == null)
            this.customContextMap = new HashMap<>();
        this.customContextMap.putAll(customContexts);
    }

    static String getAnonymousId() {
        return _anonymousId;
    }

    static void updateAnonymousId(@NonNull String anonymousId) {
        _anonymousId = anonymousId;
    }

    void setSession(RudderUserSession userSession) {
        this.sessionId = userSession.getSessionId();
        if (userSession.getSessionStart()) {
            this.sessionStart = Boolean.TRUE;
            userSession.setSessionStart(false);
        }
    }

    RudderContext copy() {
        RudderContext copy = new RudderContext();

        copy.app = this.app;
        if (this.traits != null) {
            copy.traits = new HashMap<>(this.traits);
        }
        copy.libraryInfo = this.libraryInfo;
        copy.osInfo = this.osInfo;
        copy.screenInfo = this.screenInfo;
        copy.userAgent = this.userAgent;
        copy.locale = this.locale;
        copy.deviceInfo = this.deviceInfo;
        copy.networkInfo = this.networkInfo;
        copy.timezone = this.timezone;
        if (this.externalIds != null) {
            copy.externalIds = new ArrayList<>(this.externalIds);
        }

        return copy;
    }

    public void setConsentManagement(@Nullable ConsentManagement consentManagement) {
        this.consentManagement = consentManagement;
    }

    public static class ConsentManagement {
        @SerializedName("deniedConsentIds")
        private List<String> deniedConsentIds;

        public ConsentManagement(List<String> deniedConsentIds) {
            this.deniedConsentIds = deniedConsentIds;
        }
    }
}
