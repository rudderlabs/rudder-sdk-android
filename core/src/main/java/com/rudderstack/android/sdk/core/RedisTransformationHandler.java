package com.rudderstack.android.sdk.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

interface RedisTransformationHandlerInterface {
    RedisStoreResponse makeNetworkRequest();
    void handleNetworkResponse(@Nullable RedisStoreResponse redisStoreResponse);
}

class RedisTransformationHandler implements RedisTransformationHandlerInterface, Callable<RedisTransformationHandlerInterface> {
    private final RudderNetworkManager rudderNetworkManager;
    private Map<String, Object> inputData;
    @NonNull final RedisCallback callback;

    RedisTransformationHandler(RudderNetworkManager rudderNetworkManager,
                               Map<String, Object> inputData,
                               @NonNull final RedisCallback callback
    ) {
        this.rudderNetworkManager = rudderNetworkManager;
        this.inputData = inputData;
        this.callback = callback;
    }

    @Override
    public RedisStoreResponse makeNetworkRequest() {
        RedisStoreResponse redisStoreResponse = new RedisStoreResponse();

        String requestJson = String.valueOf(createDeviceTransformPayload(inputData));
        // TODO: Replace it with url:
        String url = "<Your_URL";
        RudderNetworkManager.Result result = rudderNetworkManager.sendNetworkRequest(requestJson, url, RudderNetworkManager.RequestMethod.POST);

        Map<String, Object> returnPayload = new Gson().fromJson(
                result.response, new TypeToken<HashMap<String, Object>>() {
                }.getType()
        );
        if (returnPayload != null && returnPayload.containsKey("transformedPayload")) {
            returnPayload = (Map<String, Object>) returnPayload.get("transformedPayload");
        }
        redisStoreResponse.success = true;
        redisStoreResponse.errorMsg = result.error;
        redisStoreResponse.payload = returnPayload;
        return redisStoreResponse;
    }

    private JSONObject createDeviceTransformPayload(Map<String, Object> inputData) {
        Map<String, Object> mp = new HashMap<>();
        mp.put("payload", inputData);
        return new JSONObject(mp);
    }

    @Override
    public void handleNetworkResponse(@Nullable RedisStoreResponse redisStoreResponse) {
        callback.onResponse(redisStoreResponse);
    }

    @Override
    public RedisTransformationHandlerInterface call() {
        RedisStoreResponse redisStoreResponse = makeNetworkRequest();
        handleNetworkResponse(redisStoreResponse);
        return null;
    }
}
