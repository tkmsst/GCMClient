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

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    private static int REGISTER_DURATION = 10000;
	private static int RING_DURATION = 20000;

    static final String TAG = "GCM Client";

    private static String pushpak;
    private static String pushact;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final SharedPreferences prefs = getSharedPreferences("gcmclient", Context.MODE_PRIVATE);
        pushpak = prefs.getString("push_pak", "");
        pushact = prefs.getString("push_act", "");
        final boolean pushon = prefs.getBoolean("push_on", true);
        final boolean pushnotif = prefs.getBoolean("push_notification", true);

        if (pushnotif) {
            final String notifact = prefs.getString("notification_act", "");
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            // The getMessageType() intent parameter must be the intent you received
            // in your BroadcastReceiver.
            String messageType = gcm.getMessageType(intent);

            // Send a notification.
            if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    sendNotification(getString(R.string.send_error) + ": " + extras.toString(), notifact);
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                    sendNotification(getString(R.string.deleted) + ": " + extras.toString(),
                            notifact);
                    // If it's a regular GCM message, do some work.
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    // Post notification of received message.
                    sendNotification(
                            getString(R.string.received, extras.getString("name"), extras.getString("num")),
                            notifact);
                    Log.i(TAG, "Received: " + extras.toString());
                }
            }
        }

        // End if push is not enabled.
        if (!pushon) {
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            GcmBroadcastReceiver.completeWakefulIntent(intent);
            return;
        }

        final boolean fullwake = prefs.getBoolean("full_wake", false);
        final boolean endoff = prefs.getBoolean("end_off", true);

        // Manage the screen.
        PowerManager mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        boolean misScreenOn = mPowerManager.isScreenOn();
        int mScreenTimeout = 0;
        if (!misScreenOn) {
            if (endoff) {
                // Change the screen timeout setting.
                mScreenTimeout = Settings.System.getInt(getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, 0);
                if (mScreenTimeout != 0) {
                    Settings.System.putInt(getContentResolver(),
                            Settings.System.SCREEN_OFF_TIMEOUT, 3000);
                }
            }
            // Full wake lock
            if (fullwake) {
                mWakeLock.acquire();
            }
        }

        // Start the activity.
        try {
            startActivity(getPushactIntent(
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION |
                    Intent.FLAG_ACTIVITY_NO_ANIMATION |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
            // Wait to register.
            Thread.sleep(REGISTER_DURATION);
        } catch (android.content.ActivityNotFoundException e) {
            RING_DURATION = 0;
            Log.i(TAG, "Activity not started");
        } catch (InterruptedException e) {
        }

        // Release the wake lock.
        if (!misScreenOn && fullwake) {
            mWakeLock.release();
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);

        // Restore the screen timeout setting.
        if (endoff && mScreenTimeout != 0) {
            try {
                Thread.sleep(RING_DURATION);
            } catch (InterruptedException e) {
            }
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, mScreenTimeout);
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, String action) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = getPushactIntent(0);
        if (!action.isEmpty()) {
            intent.setAction(action);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this).
                        setSmallIcon(R.drawable.ic_stat_gcm).
                        setContentTitle(getString(R.string.notification)).
                        setStyle(new NotificationCompat.BigTextStyle().bigText(msg)).
                        setContentText(msg).
                        setAutoCancel(true).
                        setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }

    private Intent getPushactIntent(int flags) {
        Intent intent = new Intent();
        intent.setClassName(pushpak, pushact);
        intent.setFlags(flags |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
