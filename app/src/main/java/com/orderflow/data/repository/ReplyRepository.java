package com.orderflow.data.repository;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.orderflow.data.model.Reply;

import java.util.ArrayList;
import java.util.List;

/**
 * REPLY REPOSITORY
 *
 * Handles CRUD operations for Reply templates in Firestore.
 */
public class ReplyRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface ReplyListCallback {
        void onDataLoaded(List<Reply> replies);
        void onError(String errorMessage);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface ReplyFetchCallback {
        void onSuccess(Reply reply);
        void onError(String errorMessage);
    }

    private CollectionReference getReplyCollection() {
        if (auth.getCurrentUser() == null) {
            throw new IllegalStateException("User not logged in");
        }
        return db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("replies");
    }

    /**
     * Sets up a real-time listener for the replies collection, ordered by title.
     */
    public ListenerRegistration listenToReplies(ReplyListCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated.");
            return null;
        }

        return getReplyCollection()
                .orderBy("title", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }

                    List<Reply> replies = new ArrayList<>();
                    if (value != null) {
                        replies = value.toObjects(Reply.class);
                    }
                    callback.onDataLoaded(replies);
                });
    }

    /**
     * One-time fetch for a specific Reply template by ID.
     * Used by the Notification Listener to fetch the content of the matched reply.
     */
    public void getReply(String replyId, ReplyFetchCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated.");
            return;
        }

        getReplyCollection().document(replyId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Reply reply = documentSnapshot.toObject(Reply.class);
                        callback.onSuccess(reply);
                    } else {
                        callback.onError("Reply template not found.");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addReply(Reply reply, OperationCallback callback) {
        reply.setUpdatedAt(Timestamp.now());
        
        getReplyCollection().add(reply)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateReply(Reply reply, OperationCallback callback) {
        if (reply.getId() == null) {
            callback.onError("Cannot update reply: ID is null");
            return;
        }

        reply.setUpdatedAt(Timestamp.now());

        getReplyCollection().document(reply.getId()).set(reply)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteReply(String replyId, OperationCallback callback) {
        getReplyCollection().document(replyId).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
