/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.android.gcm.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Main UI for the demo app.
 */
public class MainActivity extends Activity {

    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_SEND_ID = "sender_id";
    private static final String PROPERTY_SERV_URL = "server_url";
    private static final String PROPERTY_PUSH_PAK = "push_pak";
    private static final String PROPERTY_PUSH_ACT = "push_act";
    private static final String PROPERTY_NOTIF_ACT = "notification_act";
    private static final String PROPERTY_PUSH_ON = "push_on";
    private static final String PROPERTY_PUSH_NOTIF = "push_notification";
    private static final String PROPERTY_FULL_WAKE = "full_wake";
    private static final String PROPERTY_END_OFF = "end_off";
    private static final String PROTOCOL = "http";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Client";

    private TextView mDisplay;
    private EditText mRegid;
    private EditText editText1, editText2, editText3, editText4;
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4;
    private GoogleCloudMessaging gcm;
    private Context context;
    private SharedPreferences prefs;

    String regid;
    String senderid;
    String serverurl;
    String pushpak;
    String pushact;
    String notifact;
    boolean pushon;
    boolean pushnotif;
    boolean fullwake;
    boolean endoff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        mDisplay = (TextView) findViewById(R.id.display);
        mRegid = (EditText) findViewById(R.id.regid);

        context = getApplicationContext();
        prefs = getGcmPreferences(context);

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            setParameters();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    private void storeRegistrationId(String regId) {
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(senderid);
                    msg = getString(R.string.registered);

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(regid);
                } catch (IOException ex) {
                    msg = getString(R.string.error) + " :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                mDisplay.setText(msg);
                mRegid.setText(regid);
            }
        }.execute(null, null, null);
    }

    // Send an upstream message.
    public void onClick(final View view) {

        if (view == findViewById(R.id.set)) {
            if (storeParameters()) {
                mDisplay.setText(getString(R.string.stored));
            }
        } else if (view == findViewById(R.id.register)) {
            if (!storeParameters()) {
                return;
            }
            registerInBackground();
        } else if (view == findViewById(R.id.send)) {
            if (!storeParameters()) {
                return;
            }
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "";
                    sendRegistrationIdToBackend();
                    msg = getString(R.string.sent);
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    mDisplay.setText(msg);
                    mRegid.setText(regid);
                }
            }.execute(null, null, null);
        } else if (view == findViewById(R.id.clear)) {
            mDisplay.setText("");
            mRegid.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences("gcmclient", Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        String fserverurl;
        if (serverurl.startsWith(PROTOCOL)) {
            fserverurl = serverurl;
        } else {
            fserverurl = PROTOCOL + "://" + serverurl;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("regid", regid);
        try {
            post(fserverurl, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void post(String endpoint, Map<String, String> params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IOException("Invalid URL: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // Constructs the POST body using the parameters.
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // Post the request.
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // Handle the response.
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void setParameters() {
        senderid  = prefs.getString(PROPERTY_SEND_ID, "");
        serverurl = prefs.getString(PROPERTY_SERV_URL, "");
        pushact   = prefs.getString(PROPERTY_PUSH_ACT, "");
        notifact  = prefs.getString(PROPERTY_NOTIF_ACT, "");
        pushon    = prefs.getBoolean(PROPERTY_PUSH_ON, true);
        pushnotif = prefs.getBoolean(PROPERTY_PUSH_NOTIF, true);
        fullwake  = prefs.getBoolean(PROPERTY_FULL_WAKE, false);
        endoff    = prefs.getBoolean(PROPERTY_END_OFF, true);

        editText1 = (EditText) findViewById(R.id.senderid);
        editText2 = (EditText) findViewById(R.id.serverurl);
        editText3 = (EditText) findViewById(R.id.pushact);
        editText4 = (EditText) findViewById(R.id.notifact);
        checkBox1 = (CheckBox) findViewById(R.id.pushon);
        checkBox2 = (CheckBox) findViewById(R.id.pushnotif);
        checkBox3 = (CheckBox) findViewById(R.id.fullwake);
        checkBox4 = (CheckBox) findViewById(R.id.endoff);

        editText1.setText(senderid);
        editText2.setText(serverurl);
        editText3.setText(pushact);
        editText4.setText(notifact);
        checkBox1.setChecked(pushon);
        checkBox2.setChecked(pushnotif);
        checkBox3.setChecked(fullwake);
        checkBox4.setChecked(endoff);
    }

    private boolean storeParameters() {
        senderid = editText1.getText().toString();
        if (senderid.isEmpty()) {
            mDisplay.setText(getString(R.string.no_sender_id));
            return false;
        }

        serverurl = editText2.getText().toString();

        pushpak   = "";
        pushact   = editText3.getText().toString();
        if (!pushact.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            PackageManager mPackageManager = this.getPackageManager();
            List<ResolveInfo> activitylist = mPackageManager.queryIntentActivities(intent, 0);
            for (ResolveInfo activity : activitylist) {
                if (activity.activityInfo.name.equals(pushact) ) {
                    pushpak = activity.activityInfo.packageName;
                    break;
                }
            }
            if (pushpak.isEmpty()) {
                mDisplay.setText(getString(R.string.no_activity));
                return false;
            }
        }

        notifact  = editText4.getText().toString();
        pushon    = checkBox1.isChecked();
        pushnotif = checkBox2.isChecked();
        fullwake  = checkBox3.isChecked();
        endoff    = checkBox4.isChecked();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_SEND_ID, senderid);
        editor.putString(PROPERTY_SERV_URL, serverurl);
        editor.putString(PROPERTY_PUSH_PAK, pushpak);
        editor.putString(PROPERTY_PUSH_ACT, pushact);
        editor.putString(PROPERTY_NOTIF_ACT, notifact);
        editor.putBoolean(PROPERTY_PUSH_ON, pushon);
        editor.putBoolean(PROPERTY_PUSH_NOTIF, pushnotif);
        editor.putBoolean(PROPERTY_FULL_WAKE, fullwake);
        editor.putBoolean(PROPERTY_END_OFF, endoff);
        editor.commit();

        return true;
    }
}
