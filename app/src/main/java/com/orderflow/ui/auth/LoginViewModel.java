package com.orderflow.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.orderflow.data.local.SharedPreferencesManager;
import com.orderflow.data.repository.AuthRepository;
import com.orderflow.utils.Resource;

/**
 * LOGIN VIEW MODEL
 *
 * Purpose:
 * Handles the business logic for the Login screen.
 * Isolates Firebase Auth calls from the UI layer.
 *
 * Architecture (MVVM):
 * - UI (LoginActivity) observes the LiveData objects here.
 * - When the user clicks "Sign In", the Activity calls login().
 * - This ViewModel calls the AuthRepository.
 * - The Repository returns success/failure via callback.
 * - This ViewModel updates the LiveData, which triggers UI updates
 *   (e.g., showing a progress spinner, routing to next screen, or showing error).
 */
public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final SharedPreferencesManager prefs;

    // ─────────────────────────────────────────────────────────────────────────
    // LIVE DATA
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Holds the state of the login operation.
     * Uses the Resource<T> wrapper to represent Loading, Success, or Error.
     * T is Boolean (just a flag indicating success).
     */
    private final MutableLiveData<Resource<Boolean>> loginState = new MutableLiveData<>();

    /**
     * Holds the state of the password reset operation.
     */
    private final MutableLiveData<Resource<Boolean>> resetState = new MutableLiveData<>();

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────

    public LoginViewModel() {
        this.authRepository = new AuthRepository();
        this.prefs = SharedPreferencesManager.getInstance();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC METHODS FOR UI TO OBSERVE
    // ─────────────────────────────────────────────────────────────────────────

    public LiveData<Resource<Boolean>> getLoginState() {
        return loginState;
    }

    public LiveData<Resource<Boolean>> getResetState() {
        return resetState;
    }

    /**
     * Returns the saved email if "Remember Me" was checked previously.
     * Used to pre-fill the email field when the Activity starts.
     */
    public String getSavedEmail() {
        return prefs.getSavedEmail();
    }

    /**
     * Returns whether "Remember Me" was previously checked.
     */
    public boolean getRememberMeState() {
        return prefs.isRememberMe();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC METHODS FOR UI TO TRIGGER ACTIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Attempts to sign in the user.
     *
     * @param email      User's email
     * @param password   User's password
     * @param rememberMe State of the "Remember Me" checkbox
     */
    public void login(String email, String password, boolean rememberMe) {
        // Validate inputs before making a network call
        if (email == null || email.trim().isEmpty()) {
            loginState.setValue(Resource.error("Email cannot be empty", null));
            return;
        }
        if (password == null || password.isEmpty()) {
            loginState.setValue(Resource.error("Password cannot be empty", null));
            return;
        }

        // 1. Set state to LOADING (UI will show spinner)
        loginState.setValue(Resource.loading(null));

        // 2. Call the repository
        authRepository.signIn(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                // 3a. Save "Remember Me" preferences
                prefs.setRememberMe(rememberMe);
                if (rememberMe) {
                    prefs.saveEmail(email.trim());
                } else {
                    prefs.saveEmail(null);
                }

                // 4a. Update state to SUCCESS (UI will route to Dashboard)
                loginState.setValue(Resource.success(true));
            }

            @Override
            public void onFailure(String errorMessage) {
                // 3b. Update state to ERROR (UI will show Snackbar with message)
                loginState.setValue(Resource.error(errorMessage, null));
            }
        });
    }

    /**
     * Sends a password reset email.
     * Used by ForgotPasswordActivity.
     */
    public void sendPasswordReset(String email) {
        if (email == null || email.trim().isEmpty()) {
            resetState.setValue(Resource.error("Please enter your email address", null));
            return;
        }

        resetState.setValue(Resource.loading(null));

        authRepository.sendPasswordResetEmail(email, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                resetState.setValue(Resource.success(true));
            }

            @Override
            public void onFailure(String errorMessage) {
                resetState.setValue(Resource.error(errorMessage, null));
            }
        });
    }

    /**
     * Clears the reset state.
     * Called when the UI has finished showing the success message, so it
     * doesn't re-trigger on configuration changes (like screen rotation).
     */
    public void clearResetState() {
        resetState.setValue(null);
    }
}
