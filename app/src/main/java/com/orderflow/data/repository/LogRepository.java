package com.orderflow.data.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orderflow.data.model.MessageLog;

/**
 * LOG REPOSITORY
 *
 * Purpose:
 * Saves processed messages (whether replied, ignored, or blocked by cooldown)
 * to Firestore. This provides an audit trail for the business owner and powers
 * the Statistics screen.
 */
public class LogRepository {

    private static final String TAG = "LogRepository";
    
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface OperationCallback {
        void onSuccess(String logId);
        void onError(String errorMessage);
    }

    public interface LogListCallback {
        void onDataLoaded(java.util.List<MessageLog> logs);
        void onError(String errorMessage);
    }

    private CollectionReference getLogCollection() {
        if (auth.getCurrentUser() == null) {
            throw new IllegalStateException("User not logged in");
        }
        return db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("logs");
    }

    /**
     * Saves a new message log entry to Firestore.
     * 
     * @param log      The MessageLog object to save
     * @param callback Callback returning the new Document ID upon success
     */
    public void addLog(MessageLog log, OperationCallback callback) {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot save log: User is not authenticated.");
            if (callback != null) callback.onError("User not authenticated.");
            return;
        }

        getLogCollection().add(log)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Log saved successfully: " + documentReference.getId());
                    if (callback != null) {
                        callback.onSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save log", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Listens to logs collection in real time, ordered by timestamp DESC.
     */
    public com.google.firebase.firestore.ListenerRegistration listenToLogs(LogListCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated.");
            return null;
        }

        return getLogCollection()
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }

                    java.util.List<MessageLog> logs = new java.util.ArrayList<>();
                    if (value != null) {
                        logs = value.toObjects(MessageLog.class);
                    }
                    callback.onDataLoaded(logs);
                });
    }
}
