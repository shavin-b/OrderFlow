package com.orderflow.autoresponder.presentation.settings

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orderflow.autoresponder.core.security.SecureStorage
import com.orderflow.autoresponder.core.security.TokenValidator
import com.orderflow.autoresponder.core.util.Result
import com.orderflow.autoresponder.data.repository.MetaAccountInfo
import com.orderflow.autoresponder.data.repository.MetaCredentialRepository
import com.orderflow.autoresponder.data.repository.MetaPhoneNumber
import com.orderflow.autoresponder.domain.model.MetaCredentials
import com.orderflow.autoresponder.domain.repository.WhatsAppCloudApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SettingsUiState(
    // Persisted credentials
    val credentials: MetaCredentials = MetaCredentials(),

    // Legacy test connection
    val isTestingConnection: Boolean = false,
    val testResultMessage: String? = null,
    val isTestSuccess: Boolean = false,
    val validationErrorMessage: String? = null,
    val isNotificationAccessGranted: Boolean = false,
    val isBatteryOptimizationDisabled: Boolean = false,
    val useCloudApi: Boolean = false,

    // ── Wizard state ──────────────────────────────────────────────────────────
    /** Current active wizard step (1..4) */
    val wizardStep: Int = 1,

    // Step 1 – Token verification
    val isVerifyingToken: Boolean = false,
    val tokenVerified: Boolean = false,
    val verifiedAccount: MetaAccountInfo? = null,
    val tokenError: String? = null,

    // Step 2 – Phone numbers
    val isFetchingPhoneNumbers: Boolean = false,
    val fetchedPhoneNumbers: List<MetaPhoneNumber> = emptyList(),
    val selectedPhoneNumber: MetaPhoneNumber? = null,
    val phoneNumberError: String? = null,

    // Step 3 – Business account (auto-filled)
    val isFetchingBusinessId: Boolean = false,

    // Step 4 – Webhook token
    /** Whether to show the in-app WebView sheet */
    val showWebViewSheet: Boolean = false,

    /** Snackbar / banner message */
    val bannerMessage: String? = null,
    val bannerIsSuccess: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStorage: SecureStorage,
    private val cloudApiRepository: WhatsAppCloudApiRepository,
    private val metaCredentialRepository: MetaCredentialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        checkNotificationAccess()
        checkBatteryOptimization()
    }

    // ── Existing functions ────────────────────────────────────────────────────

    fun checkNotificationAccess() {
        val packageName = context.packageName
        val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val isGranted = listeners?.contains(packageName) == true
        _uiState.value = _uiState.value.copy(isNotificationAccessGranted = isGranted)
    }

    fun checkBatteryOptimization() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        val isIgnoring = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        _uiState.value = _uiState.value.copy(isBatteryOptimizationDisabled = isIgnoring)
    }

    private fun loadSettings() {
        val creds = secureStorage.getMetaCredentials()
        val useCloud = secureStorage.useCloudApi()
        // If credentials already exist, mark token as verified so wizard shows them
        val hasToken = creds.accessToken.isNotBlank()
        _uiState.value = _uiState.value.copy(
            credentials = creds,
            useCloudApi = useCloud,
            tokenVerified = hasToken,
            wizardStep = if (hasToken) 4 else 1
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
            bannerMessage = "Credentials saved successfully!",
            bannerIsSuccess = true
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

    // ── Wizard functions ──────────────────────────────────────────────────────

    /** Step 1: Verify an access token and, on success, auto-fetch phone numbers */
    fun verifyToken(token: String) {
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(tokenError = "Access token cannot be empty")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isVerifyingToken = true,
                tokenError = null,
                tokenVerified = false,
                verifiedAccount = null
            )
            when (val result = metaCredentialRepository.verifyToken(token)) {
                is Result.Success -> {
                    // Also try fetching business ID in parallel
                    val businessResult = metaCredentialRepository.fetchBusinessAccountId(token)
                    val businessId = (businessResult as? Result.Success)?.data ?: ""

                    val updatedCreds = _uiState.value.credentials.copy(
                        accessToken = token,
                        businessAccountId = businessId
                    )
                    _uiState.value = _uiState.value.copy(
                        isVerifyingToken = false,
                        tokenVerified = true,
                        verifiedAccount = result.data,
                        credentials = updatedCreds,
                        tokenError = null,
                        wizardStep = 2
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isVerifyingToken = false,
                        tokenVerified = false,
                        tokenError = result.message ?: "Token verification failed"
                    )
                }
                else -> {}
            }
        }
    }

    /** Step 2: Fetch WhatsApp phone numbers using the verified token */
    fun fetchPhoneNumbers() {
        val token = _uiState.value.credentials.accessToken
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(phoneNumberError = "Please verify your access token first")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isFetchingPhoneNumbers = true,
                phoneNumberError = null,
                fetchedPhoneNumbers = emptyList()
            )
            when (val result = metaCredentialRepository.fetchPhoneNumbers(token)) {
                is Result.Success -> {
                    val phones = result.data
                    // Auto-select if only one phone exists
                    val autoSelected = if (phones.size == 1) phones.first() else null
                    val updatedCreds = if (autoSelected != null) {
                        _uiState.value.credentials.copy(phoneNumberId = autoSelected.phoneNumberId)
                    } else {
                        _uiState.value.credentials
                    }
                    _uiState.value = _uiState.value.copy(
                        isFetchingPhoneNumbers = false,
                        fetchedPhoneNumbers = phones,
                        selectedPhoneNumber = autoSelected,
                        credentials = updatedCreds,
                        phoneNumberError = null,
                        wizardStep = if (autoSelected != null) 3 else 2
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isFetchingPhoneNumbers = false,
                        phoneNumberError = result.message ?: "Failed to fetch phone numbers"
                    )
                }
                else -> {}
            }
        }
    }

    /** Step 2: User manually selects a phone number from the fetched list */
    fun selectPhoneNumber(phone: MetaPhoneNumber) {
        val updatedCreds = _uiState.value.credentials.copy(phoneNumberId = phone.phoneNumberId)
        _uiState.value = _uiState.value.copy(
            selectedPhoneNumber = phone,
            credentials = updatedCreds,
            wizardStep = 3
        )
    }

    /** Step 3: Manually update business account ID */
    fun updateBusinessAccountId(id: String) {
        val updatedCreds = _uiState.value.credentials.copy(businessAccountId = id)
        _uiState.value = _uiState.value.copy(credentials = updatedCreds)
        if (id.isNotBlank()) {
            _uiState.value = _uiState.value.copy(wizardStep = maxOf(_uiState.value.wizardStep, 4))
        }
    }

    /** Step 4: Generate a random secure webhook verify token */
    fun generateWebhookToken() {
        val random = "orderflow_" + UUID.randomUUID().toString().replace("-", "").take(16)
        val updatedCreds = _uiState.value.credentials.copy(webhookVerifyToken = random)
        _uiState.value = _uiState.value.copy(credentials = updatedCreds)
    }

    /** Update webhook token manually */
    fun updateWebhookToken(token: String) {
        val updatedCreds = _uiState.value.credentials.copy(webhookVerifyToken = token)
        _uiState.value = _uiState.value.copy(credentials = updatedCreds)
    }

    /** Show the in-app WebView sheet for Meta Graph Explorer */
    fun showWebViewSheet() {
        _uiState.value = _uiState.value.copy(showWebViewSheet = true)
    }

    fun hideWebViewSheet() {
        _uiState.value = _uiState.value.copy(showWebViewSheet = false)
    }

    /** Called when user copies a token from the WebView */
    fun onTokenCopiedFromWebView(token: String) {
        _uiState.value = _uiState.value.copy(
            showWebViewSheet = false,
            credentials = _uiState.value.credentials.copy(accessToken = token)
        )
    }

    /** Navigate wizard to a specific step */
    fun goToStep(step: Int) {
        if (step in 1..4) {
            _uiState.value = _uiState.value.copy(wizardStep = step)
        }
    }

    /** Clear any banner/snackbar message */
    fun clearBanner() {
        _uiState.value = _uiState.value.copy(bannerMessage = null, testResultMessage = null)
    }

    /** Save & test in one go */
    fun saveAndTest() {
        val creds = _uiState.value.credentials
        saveCredentials(creds)
        testApiConnection(creds)
    }
}
