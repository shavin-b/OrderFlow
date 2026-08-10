package com.orderflow.autoresponder.presentation.settings

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orderflow.autoresponder.core.security.SecureStorage
import com.orderflow.autoresponder.core.security.TokenValidator
import com.orderflow.autoresponder.core.util.Result
import com.orderflow.autoresponder.domain.model.MetaCredentials
import com.orderflow.autoresponder.domain.repository.WhatsAppCloudApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val credentials: MetaCredentials = MetaCredentials(),
    val isTestingConnection: Boolean = false,
    val testResultMessage: String? = null,
    val isTestSuccess: Boolean = false,
    val validationErrorMessage: String? = null,
    val isNotificationAccessGranted: Boolean = false,
    val useCloudApi: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStorage: SecureStorage,
    private val cloudApiRepository: WhatsAppCloudApiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        checkNotificationAccess()
    }

    fun checkNotificationAccess() {
        val packageName = context.packageName
        val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val isGranted = listeners?.contains(packageName) == true
        _uiState.value = _uiState.value.copy(isNotificationAccessGranted = isGranted)
    }

    private fun loadSettings() {
        val creds = secureStorage.getMetaCredentials()
        val useCloud = secureStorage.useCloudApi()
        _uiState.value = _uiState.value.copy(
            credentials = creds,
            useCloudApi = useCloud
        )
    }

    fun setUseCloudApi(useCloud: Boolean) {
        secureStorage.setUseCloudApi(useCloud)
        _uiState.value = _uiState.value.copy(useCloudApi = useCloud)
    }

    fun saveCredentials(credentials: MetaCredentials) {
        if (!TokenValidator.isValidPhoneNumberId(credentials.phoneNumberId)) {
            _uiState.value = _uiState.value.copy(validationErrorMessage = "Invalid Phone Number ID format")
            return
        }
        if (!TokenValidator.isValidAccessToken(credentials.accessToken)) {
            _uiState.value = _uiState.value.copy(validationErrorMessage = "Invalid Meta Access Token format")
            return
        }

        secureStorage.saveMetaCredentials(credentials)
        _uiState.value = _uiState.value.copy(
            credentials = credentials,
            validationErrorMessage = null,
            testResultMessage = "Credentials saved successfully!"
        )
    }

    fun testApiConnection(credentials: MetaCredentials) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingConnection = true, testResultMessage = null)
            when (val result = cloudApiRepository.testConnection(credentials)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isTestingConnection = false,
                        testResultMessage = "Meta WhatsApp Cloud API connection verified successfully!",
                        isTestSuccess = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isTestingConnection = false,
                        testResultMessage = "Connection failed: ${result.message}",
                        isTestSuccess = false
                    )
                }
                else -> {}
            }
        }
    }
}
