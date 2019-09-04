package com.berbix.cordova;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;

import com.berbix.berbixverify.BerbixEnvironment;
import com.berbix.berbixverify.BerbixSDK;
import com.berbix.berbixverify.BerbixSDKAdapter;
import com.berbix.berbixverify.BerbixSDKOptions;
import com.berbix.berbixverify.BerbixSDKOptionsBuilder;

public class BerbixPlugin extends CordovaPlugin implements BerbixSDKAdapter {
    private static final String VERIFY = "verify";

    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        // "verify" is the only action, so this departs from the typical cordova
        // pattern a bit
        if (!VERIFY.equals(action)) {
            return false;
        }

        JSONObject config = args.optJSONObject(0);

        if (config == null) {
            callbackContext.error("cannot start berbix flow configuration");
            return true;
        }

        String clientID = config.optString("client_id");
        String templateKey = config.optString("template_key");
        String baseURL = config.optString("base_url");
        String clientToken = config.optString("client_token");
        String environment = config.optString("environment");

        if (clientID == null) {
            callbackContext.error("cannot start berbix flow without client ID");
            return true;
        }

        BerbixSDKOptionsBuilder options = new BerbixSDKOptionsBuilder();

        if (!templateKey.isEmpty()) {
            options = options.setTemplateKey(templateKey);
        }
        if (!baseURL.isEmpty()) {
            options = options.setBaseURL(baseURL);
        }
        if (!clientToken.isEmpty()) {
            options = options.setClientToken(clientToken);
        }
        if (!environment.isEmpty()) {
            BerbixEnvironment env = getEnvironment(environment);
            if (env != null) {
                options = options.setEnvironment(env);
            }
        }

        BerbixSDK sdk = new BerbixSDK(clientID);
        final BerbixSDKOptions sdkOptions = options.build();

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                sdk.startFlow(BerbixPlugin.this.cordova.getActivity(), BerbixPlugin.this, sdkOptions);
            }
        });

        this.callbackContext = callbackContext;

        return true;
    }

    @Override
    public void onComplete() {
        this.callbackContext.success();
    }

    @Override
    public void onError(Throwable error) {
        this.callbackContext.error(error.toString());
    }

    private static BerbixEnvironment getEnvironment(String environment) {
        switch (environment) {
        case "production":
            return BerbixEnvironment.PRODUCTION;
        case "staging":
            return BerbixEnvironment.STAGING;
        case "sandbox":
            return BerbixEnvironment.SANDBOX;
        default:
            return null;
        }
    }
}