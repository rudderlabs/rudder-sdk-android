package com.rudderlabs.android.sdk.core;

import com.google.gson.Gson;
import com.rudderlabs.android.sdk.core.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class RudderProperty {
    private Map<String, Object> map = new HashMap<>();

    public Object getProperty(String key) {
        return map.containsKey(key) ? map.get(key) : null;
    }

    public void setProperty(String key, Object value) {
        map.put(key, value);
    }

    public boolean hasProperty(String key) {
        return map.containsKey(key);
    }

    Map<String, Object> getMap() {
        return map;
    }

    public void setProperty(Map<String, Object> map) {
        if (map != null) this.map.putAll(map);
    }

    public void setProperty(Object property) {
        setProperty(Utils.convertToMap(new Gson().toJson(property)));
    }
}
