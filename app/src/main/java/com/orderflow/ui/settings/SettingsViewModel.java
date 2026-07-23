package com.orderflow.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.orderflow.data.local.SharedPreferencesManager;
import com.orderflow.data.model.UserSettings;
import com.orderflow.data.repository.SettingsRepository;
import com.orderflow.utils.Resource;

/**
 * SETTINGS VIEW MODEL
 *
 * Manages loading, editing, and saving user preferences.
 * Ensures local SharedPreferences cache stays synchronized with Firestore.
 */
public class SettingsViewModel extends ViewModel {

    private final SettingsRepository repository;
    private final SharedPreferencesManager prefsManager;

    private final MutableLiveData<Resource<UserSettings>> settingsState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> saveState = new MutableLiveData<>();

    public SettingsViewModel() {
        repository = new SettingsRepository();
        prefsManager = SharedPreferencesManager.getInstance();
        loadSettings();
    }

    public LiveData<Resource<UserSettings>> getSettingsState() {
        return settingsState;
    }

    public LiveData<Resource<Void>> getSaveState() {
        return saveState;
    }

    public void loadSettings() {
        settingsState.setValue(Resource.loading(null));
        repository.getSettings(new SettingsRepository.SettingsCallback() {
            @Override
            public void onSuccess(UserSettings settings) {
                // Update local SharedPreferences cache
                prefsManager.applySettings(settings);
                settingsState.setValue(Resource.success(settings));
            }

            @Override
            public void onError(String errorMessage) {
                // Fall back to local SharedPreferences
                UserSettings localSettings = new UserSettings();
                localSettings.setAutoReplyEnabled(prefsManager.isAutoReplyEnabled());
                localSettings.setCooldownHours(prefsManager.getCooldownHours());
                localSettings.setDarkModeEnabled(prefsManager.isDarkModeEnabled());
                localSettings.setWizardCompleted(prefsManager.isWizardCompleted());
                localSettings.setLastBackupTimestamp(prefsManager.getLastBackupTimestamp());
                
                settingsState.setValue(Resource.success(localSettings));
            }
        });
    }

    public void updateSettings(UserSettings settings) {
        saveState.setValue(Resource.loading(null));

        // Update local SharedPreferences immediately for instantaneous response
        prefsManager.applySettings(settings);

        // Sync with Firestore in background
        repository.saveSettings(settings, new SettingsRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                saveState.setValue(Resource.success(null));
            }

            @Override
            public void onError(String errorMessage) {
                saveState.setValue(Resource.error(errorMessage, null));
            }
        });
    }

    public void clearCooldowns() {
        prefsManager.clearAllCooldowns();
    }
}
