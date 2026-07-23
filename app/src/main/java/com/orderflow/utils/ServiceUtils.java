package com.orderflow.utils;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import com.orderflow.service.AutoReplyAccessibilityService;

/**
 * Utility class for checking if the required system services are currently enabled.
 * Used by the Permission Wizard and the Dashboard.
 */
public class ServiceUtils {

    /**
     * Checks if our WhatsAppNotificationListener is currently granted access by the OS.
     */
    public static boolean isNotificationServiceEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && TextUtils.equals(pkgName, cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if our AutoReplyAccessibilityService is currently granted access by the OS.
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + AutoReplyAccessibilityService.class.getCanonicalName();

        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            // Setting not found, assume false
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
