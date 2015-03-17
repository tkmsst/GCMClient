package org.android.gcm.client;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;

/**
 * LockActivity.
 */
public class LockActivity extends Activity {

    DevicePolicyManager mDevicePolicyManager;
    ComponentName mComponentName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDevicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, LockReceiver.class);
        if (mDevicePolicyManager.isAdminActive(mComponentName)) {
                mDevicePolicyManager.lockNow();
        }
        finish();
    }
}
