package org.android.gcm.client;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * LockReceiver.
 */
public class LockReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
    }
}
