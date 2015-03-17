/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.android.gcm.client;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.List;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    private String SIP_PACKAGE  = "com.csipsimple";
    private String SIP_ACTIVITY = "com.csipsimple.ui.SipHome";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "GCM Client";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification(getString(R.string.send_error) + ": " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification(getString(R.string.deleted) + ": " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                sendNotification(getString(R.string.received, extras.getString("name")));
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Launch the SIP application.
        boolean answer = true;
        if (intent != null) {
            PowerManager mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
            boolean misScreenOn = mPowerManager.isScreenOn();
            startActivity(getSipIntent(Intent.FLAG_ACTIVITY_NO_USER_ACTION
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
            SystemClock.sleep(10000);
            if (!misScreenOn) {
                for (int count = 0; count < 5; count++) {
                    SystemClock.sleep(2000);
                    if (!checkRunningSipProcess()) {
                        answer = false;
                        SystemClock.sleep(1000);
                        break;
                    }
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
        // Lock the device.
        if (!answer) {
            Intent lockIntent = new Intent(this, LockActivity.class);
            lockIntent.setClass(getApplicationContext(), LockActivity.class);
            lockIntent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION
                    | Intent.FLAG_ACTIVITY_NO_HISTORY
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(lockIntent);
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = null;
        Intent intent = getSipIntent(0);
        if (intent != null) {
            intent.setAction("com.csipsimple.phone.action.CALLLOG");
            contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_gcm)
                        .setContentTitle(getString(R.string.notification))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private Intent getSipIntent(int flags) {
        Intent intent = new Intent();
        PackageManager mPackageManager = this.getPackageManager();
        try {
            mPackageManager.getApplicationInfo(SIP_PACKAGE, PackageManager.GET_META_DATA);
            intent.setClassName(SIP_PACKAGE, SIP_ACTIVITY);
            intent.setFlags(flags
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_ACTIVITY_NO_HISTORY
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch(NameNotFoundException e) {
            intent = null;
            Log.i(TAG, "App not found");
        }
        return intent;
    }

    private boolean checkRunningSipProcess() {
        ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfoList = mActivityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo info : processInfoList) {
            if (info.processName.equals(SIP_PACKAGE + ":sipStack")) {
                if (info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i(TAG, "App is running.");
                    return true;
                }
            }
        }
        return false;
    }
}
