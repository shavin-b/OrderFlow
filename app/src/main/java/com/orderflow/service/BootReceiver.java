package com.orderflow.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BOOT RECEIVER — PHASE 1 STUB
 *
 * This is a compile-time stub. Full implementation added when services are complete.
 *
 * Purpose:
 * Receives the BOOT_COMPLETED broadcast when the device finishes booting.
 * Ensures that the auto-reply system starts automatically after a reboot
 * without requiring the user to manually open the app.
 *
 * Note: Android 8.0+ has restrictions on background service starts from boot.
 * The full implementation will use a foreground service notification approach
 * to comply with Android's background execution limits.
 *
 * Required manifest declaration:
 * <receiver android:name=".service.BootReceiver" android:exported="false">
 *     <intent-filter>
 *         <action android:name="android.intent.action.BOOT_COMPLETED" />
 *     </intent-filter>
 * </receiver>
 *
 * Also requires permission:
 * <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || "android.intent.action.LOCKED_BOOT_COMPLETED".equals(intent.getAction())) {
            // Full implementation: restart NotificationListenerService and
            // prompt the user if accessibility service is disabled after reboot.
            // (Notification Listener service is restarted automatically by Android
            //  if the user had it enabled — this receiver handles any additional setup)
        }
    }
}
