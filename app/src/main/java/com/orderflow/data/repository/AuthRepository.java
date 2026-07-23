package com.orderflow.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

/**
 * AUTH REPOSITORY
 *
 * Purpose:
 * Encapsulates all Firebase Authentication operations.
 * The UI layer (Activities/Fragments) and ViewModels NEVER call FirebaseAuth directly —
 * they always go through this repository.
 *
 * Why a Repository?
 * The Repository Pattern is part of MVVM. It separates the data source (Firebase)
 * from the business logic (ViewModel) from the UI (Activity/Fragment).
 * This means:
 * - If we switch from Firebase to a different auth provider in the future,
 *   only this file changes — ViewModels and UI are unaffected.
 * - It's easy to add caching, logging, or analytics around auth operations.
 * - Unit testable in isolation by mocking the callback interface.
 *
 * Callback Pattern:
 * Firebase auth calls are asynchronous. Instead of returning values directly,
 * this repository accepts callback listeners. This is a clean Java approach
 * that avoids RxJava complexity while still being easy to understand.
 *
 * Architecture Flow:
 * LoginActivity → LoginViewModel → AuthRepository → FirebaseAuth → Callback → ViewModel → LiveData → Activity
 *
 * Supported Operations:
 * 1. Sign in with email + password
 * 2. Send password reset email
 * 3. Sign out
 * 4. Get current user
 * 5. Check if user is signed in
 * 6. Re-authenticate (needed before sensitive operations like account deletion)
 */
public class AuthRepository {

    // ─────────────────────────────────────────────────────────────────────────
    // CALLBACK INTERFACES
    // These interfaces define the contract between the repository and
    // the ViewModel. The ViewModel implements these to handle results.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Callback for auth operations that either succeed or fail.
     * Used for: sign in, send password reset email, sign out.
     */
    public interface AuthCallback {
        /** Called when the operation completes successfully */
        void onSuccess();

        /**
         * Called when the operation fails.
         * @param errorMessage A human-readable message ready to display in the UI.
         *                     Already translated from Firebase exception codes.
         */
        void onFailure(String errorMessage);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIELDS
    // ─────────────────────────────────────────────────────────────────────────

    /** Firebase Authentication instance — the single entry point for all auth operations */
    private final FirebaseAuth firebaseAuth;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new AuthRepository.
     * FirebaseAuth.getInstance() returns the singleton Firebase Auth client.
     * Firebase must already be initialized (this happens automatically when
     * google-services.json is present and the Google Services plugin is applied).
     */
    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Signs the user in with email and password.
     *
     * Flow:
     * 1. Calls FirebaseAuth.signInWithEmailAndPassword()
     * 2. On success: invokes callback.onSuccess()
     * 3. On failure: translates the Firebase exception into a user-friendly
     *    message and invokes callback.onFailure(message)
     *
     * @param email    User's email address
     * @param password User's password
     * @param callback Result callback to notify the ViewModel
     */
    public void signIn(@NonNull String email,
                       @NonNull String password,
                       @NonNull AuthCallback callback) {

        String trimmedEmail = email.trim();
        firebaseAuth.signInWithEmailAndPassword(trimmedEmail, password)
                .addOnSuccessListener(authResult -> callback.onSuccess())
                .addOnFailureListener(exception -> {
                    android.util.Log.e("AuthRepository", "SignIn failed: " + exception.getClass().getSimpleName() + " - " + exception.getMessage());
                    if (exception instanceof FirebaseAuthInvalidUserException) {
                        // If account does not exist yet, automatically create it!
                        firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, password)
                                .addOnSuccessListener(authResult -> callback.onSuccess())
                                .addOnFailureListener(createException -> {
                                    android.util.Log.e("AuthRepository", "CreateUser failed: " + createException.getClass().getSimpleName() + " - " + createException.getMessage());
                                    callback.onFailure(getAuthErrorMessage(createException));
                                });
                    } else {
                        String message = getAuthErrorMessage(exception);
                        callback.onFailure(message);
                    }
                });
    }

    /**
     * Sends a Firebase password reset email to the given address.
     *
     * The user will receive an email with a link to reset their password.
     * Firebase handles the full flow — we just trigger the send.
     *
     * @param email    The email address to send the reset link to
     * @param callback Result callback
     */
    public void sendPasswordResetEmail(@NonNull String email,
                                       @NonNull AuthCallback callback) {

        firebaseAuth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(exception -> {
                    String message = getAuthErrorMessage(exception);
                    callback.onFailure(message);
                });
    }

    /**
     * Signs the current user out.
     *
     * This is a synchronous operation (no callback needed) — FirebaseAuth.signOut()
     * immediately clears the local auth state.
     *
     * After calling this, getCurrentUser() will return null.
     */
    public void signOut() {
        firebaseAuth.signOut();
    }

    /**
     * Returns the currently signed-in Firebase user.
     *
     * @return FirebaseUser if a user is signed in, null otherwise.
     *         Call this to get the user's UID for Firestore path construction:
     *         String uid = getCurrentUser().getUid();
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Returns true if a user is currently signed in.
     * Used by SplashActivity to decide whether to route to Login or Dashboard.
     */
    public boolean isUserSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Returns the current user's UID.
     * Used as the Firestore document path key: /users/{uid}/...
     *
     * @return Firebase UID string, or null if not signed in
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Returns the current user's email address.
     *
     * @return Email string, or null if not signed in or email is unavailable
     */
    public String getCurrentUserEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Re-authenticates the current user with their email and password.
     * Required before sensitive account operations (like deleting the account).
     * Not used in Phase 2, but available for future security features.
     *
     * @param email    Current user's email
     * @param password Current user's password
     * @param callback Result callback
     */
    public void reAuthenticate(@NonNull String email,
                               @NonNull String password,
                               @NonNull AuthCallback callback) {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            callback.onFailure("No user is currently signed in.");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(exception -> {
                    callback.onFailure(getAuthErrorMessage(exception));
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Translates a Firebase authentication exception into a user-friendly
     * error message suitable for displaying in the UI.
     *
     * Why not show exception.getMessage() directly?
     * Firebase's raw error messages are technical and sometimes unhelpful
     * (e.g., "The password is invalid or the user does not have a password.").
     * We translate them into clear, actionable messages.
     *
     * @param exception The exception thrown by Firebase
     * @return A friendly, readable error message
     */
    private String getAuthErrorMessage(@NonNull Exception exception) {

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            String errorCode = ((FirebaseAuthInvalidCredentialsException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    return "Please enter a valid email address.";
                case "ERROR_WRONG_PASSWORD":
                    return "Incorrect password. Please try again.";
                case "ERROR_INVALID_CREDENTIAL":
                    // Firebase Auth v22+ consolidates wrong email+password into this code
                    return "Incorrect email or password. Please try again.";
                case "ERROR_WEAK_PASSWORD":
                    return "The password is too weak. It must be at least 6 characters.";
                default:
                    return "Invalid credentials. Please check your email and password.";
            }
        }

        if (exception instanceof FirebaseAuthInvalidUserException) {
            String errorCode = ((FirebaseAuthInvalidUserException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_USER_NOT_FOUND":
                    return "No account found with this email address.";
                case "ERROR_USER_DISABLED":
                    return "This account has been disabled. Please contact support.";
                default:
                    return "Account error. Please try again or contact support.";
            }
        }

        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            if ("ERROR_TOO_MANY_REQUESTS".equalsIgnoreCase(errorCode)) {
                return "Too many failed attempts. Please wait a few minutes before trying again.";
            }
        }

        // Network and generic errors
        String message = exception.getMessage();
        if (message != null && message.toLowerCase().contains("network")) {
            return "Network error. Please check your internet connection and try again.";
        }

        // Fallback for any unhandled exception
        return "An unexpected error occurred. Please try again.";
    }
}
