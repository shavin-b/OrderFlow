package com.orderflow.data.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.orderflow.data.local.SharedPreferencesManager;
import com.orderflow.data.model.UserSettings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * BACKUP MANAGER
 *
 * Coordinates data synchronization and explicit backup operations to Firebase.
 */
public class BackupManager {

    private static final String TAG = "BackupManager";

    public interface BackupCallback {
        void onSuccess(String timestamp);
        void onError(String errorMessage);
    }

    private final SettingsRepository settingsRepository;

    public BackupManager() {
        this.settingsRepository = new SettingsRepository();
    }

    /**
     * Performs a full cloud backup verification by updating the UserSettings backup timestamp
     * and syncing local SharedPreferences with Firestore.
     */
    public void performFullBackup(BackupCallback callback) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            if (callback != null) callback.onError("User not authenticated.");
            return;
        }

        String nowIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        settingsRepository.getSettings(new SettingsRepository.SettingsCallback() {
            @Override
            public void onSuccess(UserSettings settings) {
                settings.setLastBackupTimestamp(nowIso);
                settingsRepository.saveSettings(settings, new SettingsRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        SharedPreferencesManager.getInstance().setLastBackupTimestamp(nowIso);
                        Log.d(TAG, "Full backup timestamp saved to Firestore: " + nowIso);
                        if (callback != null) callback.onSuccess(nowIso);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to save backup timestamp: " + errorMessage);
                        if (callback != null) callback.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to fetch settings for backup: " + errorMessage);
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }
}
