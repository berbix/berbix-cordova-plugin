package com.berbix.cordova;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;

import android.content.Intent;
import android.os.Bundle;

import com.berbix.berbixverify.BerbixSDK;
import com.berbix.berbixverify.BerbixConfiguration;
import com.berbix.berbixverify.BerbixConfigurationBuilder;
import com.berbix.berbixverify.BerbixConstants;
import com.berbix.berbixverify.BerbixResultStatus;
import com.berbix.berbixverify.activities.BerbixActivity;

public class BerbixPlugin extends CordovaPlugin {
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

        String baseURL = config.optString("base_url");
        String clientToken = config.optString("client_token");
        boolean debug = config.optBoolean("debug", false);

        BerbixConfigurationBuilder optionsBuilder = new BerbixConfigurationBuilder();

        if (!baseURL.isEmpty()) {
            optionsBuilder = optionsBuilder.setBaseURL(baseURL);
        }
        if (!clientToken.isEmpty()) {
            optionsBuilder = optionsBuilder.setClientToken(clientToken);
        }
        if (debug) {
            optionsBuilder = optionsBuilder.setDebug(debug);
        }

        final BerbixConfiguration options = optionsBuilder.build();
        BerbixSDK sdk = new BerbixSDK();

        cordova.setActivityResultCallback(this);

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Intent intent = new Intent(BerbixPlugin.this.cordova.getActivity(), BerbixActivity.class);
                intent.putExtra("config", options);
                BerbixPlugin.this.cordova.startActivityForResult(BerbixPlugin.this, intent, BerbixConstants.REQUEST_CODE_BERBIX_FLOW);
            }
        });

        this.callbackContext = callbackContext;

        return true;
    }

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == BerbixConstants.REQUEST_CODE_BERBIX_FLOW) {
            switch (BerbixResultStatus.getStatus(resultCode)) {
                case SUCCESS:
                    this.callbackContext.success();
                    break;
                case USER_EXIT:
                    this.callbackContext.error("userExitError");
                    break;
                case NO_CAMERA_PERMISSIONS:
                case UNABLE_TO_LAUNCH_CAMERA:
                    this.callbackContext.error("cameraAccessError");
                    break;
                case ERROR:
                    this.callbackContext.error("apiError");
                    break;
                case UNKNOWN_ERROR:
                    // There appears to be an android issue whereby a 0 result can be returned
                    // immediately upon creating the activity. We ignore those to see if that
                    // addresses the underlying issue.
                    break;
            }
        }
    }
}