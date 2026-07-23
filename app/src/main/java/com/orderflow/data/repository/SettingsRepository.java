package com.orderflow.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orderflow.data.model.UserSettings;

/**
 * SETTINGS REPOSITORY
 *
 * Manages fetching and updating the single UserSettings document in Firestore.
 * Document Path: /users/{userId}/settings/config
 */
public class SettingsRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface SettingsCallback {
        void onSuccess(UserSettings settings);
        void onError(String errorMessage);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    private DocumentReference getSettingsDocument() {
        if (auth.getCurrentUser() == null) {
            throw new IllegalStateException("User not logged in");
        }
        return db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("settings")
                .document("config");
    }

    /**
     * One-time fetch of user settings. If none exist, returns default UserSettings.
     */
    public void getSettings(SettingsCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated.");
            return;
        }

        getSettingsDocument().get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserSettings settings = documentSnapshot.toObject(UserSettings.class);
                        callback.onSuccess(settings != null ? settings : new UserSettings());
                    } else {
                        // Create default settings if document does not exist yet
                        UserSettings defaultSettings = new UserSettings();
                        saveSettings(defaultSettings, new OperationCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess(defaultSettings);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                callback.onSuccess(defaultSettings); // Fallback to memory default
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Saves user settings to Firestore.
     */
    public void saveSettings(UserSettings settings, OperationCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated.");
            return;
        }

        getSettingsDocument().set(settings)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
