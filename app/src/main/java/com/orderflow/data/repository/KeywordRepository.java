package com.orderflow.data.repository;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.orderflow.data.model.Keyword;

import java.util.ArrayList;
import java.util.List;

/**
 * KEYWORD REPOSITORY
 *
 * Purpose:
 * Handles all CRUD operations for the Keyword model in Firestore.
 * Abstracts Firebase logic away from the ViewModels.
 */
public class KeywordRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface KeywordListCallback {
        void onDataLoaded(List<Keyword> keywords);
        void onError(String errorMessage);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * Gets the CollectionReference for the current user's keywords.
     * Path: /users/{userId}/keywords
     */
    private CollectionReference getKeywordCollection() {
        if (auth.getCurrentUser() == null) {
            throw new IllegalStateException("User not logged in");
        }
        return db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("keywords");
    }

    /**
     * Sets up a real-time listener for the keywords collection, ordered by priority.
     * Returns the ListenerRegistration so the ViewModel can detach it when destroyed.
     */
    public ListenerRegistration listenToKeywords(KeywordListCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated.");
            return null;
        }

        return getKeywordCollection()
                .orderBy("priority", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }

                    List<Keyword> keywords = new ArrayList<>();
                    if (value != null) {
                        // Firestore automatically deserializes the documents into Keyword objects.
                        // The @DocumentId annotation in the Keyword model populates the 'id' field.
                        keywords = value.toObjects(Keyword.class);
                    }
                    callback.onDataLoaded(keywords);
                });
    }

    /**
     * One-time fetch of all active keywords, ordered by priority.
     * Used by the Notification Listener in the background.
     */
    public void getActiveKeywords(KeywordListCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated.");
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        android.util.Log.d("KeywordRepository", "Fetching active keywords for UID: " + uid);

        getKeywordCollection()
                // Temporarily removing filter to diagnose empty result issue
                .orderBy("priority", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Keyword> allKeywords = queryDocumentSnapshots.toObjects(Keyword.class);
                    android.util.Log.d("KeywordRepository", "Total keywords found in DB: " + allKeywords.size());
                    
                    List<Keyword> activeKeywords = new java.util.ArrayList<>();
                    for (Keyword k : allKeywords) {
                        android.util.Log.d("KeywordRepository", "Rule: " + k.getId() + " | Enabled: " + k.isEnabled());
                        if (k.isEnabled()) {
                            activeKeywords.add(k);
                        }
                    }
                    callback.onDataLoaded(activeKeywords);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("KeywordRepository", "Error fetching active keywords: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Adds a new keyword to Firestore.
     * Uses a generated document ID.
     */
    public void addKeyword(Keyword keyword, OperationCallback callback) {
        keyword.setUpdatedAt(Timestamp.now());
        
        getKeywordCollection().add(keyword)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Updates an existing keyword in Firestore.
     */
    public void updateKeyword(Keyword keyword, OperationCallback callback) {
        if (keyword.getId() == null) {
            callback.onError("Cannot update keyword: ID is null");
            return;
        }

        keyword.setUpdatedAt(Timestamp.now());

        getKeywordCollection().document(keyword.getId()).set(keyword)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Deletes a keyword from Firestore.
     */
    public void deleteKeyword(String keywordId, OperationCallback callback) {
        getKeywordCollection().document(keywordId).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
