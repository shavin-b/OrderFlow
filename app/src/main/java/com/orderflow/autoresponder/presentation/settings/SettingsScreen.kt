package com.orderflow.autoresponder.presentation.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReplyAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orderflow.autoresponder.domain.model.MetaCredentials
import com.orderflow.autoresponder.presentation.components.AppTopBar
import com.orderflow.autoresponder.presentation.theme.BrandAccent
import com.orderflow.autoresponder.presentation.theme.BrandDarkBackground
import com.orderflow.autoresponder.presentation.theme.BrandGreen
import com.orderflow.autoresponder.presentation.theme.BrandTextPrimary
import com.orderflow.autoresponder.presentation.theme.BrandTextSecondary
import com.orderflow.autoresponder.presentation.theme.StatusFailed
import com.orderflow.autoresponder.presentation.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.checkNotificationAccess()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        containerColor = BrandDarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Automation Trigger",
                style = MaterialTheme.typography.titleLarge,
                color = BrandTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            PermissionRow(
                title = "Notification Access",
                isGranted = state.isNotificationAccessGranted,
                onGrantClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionRow(
                title = "Battery Optimization",
                isGranted = state.isBatteryOptimizationDisabled,
                onGrantClick = {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Reply Method Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandTextSecondary.copy(alpha = 0.1f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrandAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.useCloudApi) Icons.Default.Cloud else Icons.Default.ReplyAll,
                        contentDescription = null,
                        tint = BrandAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reply Method",
                        color = BrandTextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (state.useCloudApi) "Cloud API (Official)" else "Local (Direct Reply)",
                        color = BrandTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked = state.useCloudApi,
                    onCheckedChange = { viewModel.setUseCloudApi(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BrandGreen,
                        checkedTrackColor = BrandGreen.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = BrandTextSecondary.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "WhatsApp Business Meta API Setup",
                style = MaterialTheme.typography.titleLarge,
                color = BrandTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Configure your Meta developer account to send verified WhatsApp messages.",
                style = MaterialTheme.typography.bodySmall,
                color = BrandTextSecondary
            )

            // Result / Error Banner
            val bannerText = state.testResultMessage ?: state.validationErrorMessage ?: state.bannerMessage
            val isBannerSuccess = state.isTestSuccess || (state.bannerMessage != null && state.bannerIsSuccess)
            AnimatedVisibility(
                visible = bannerText != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (bannerText != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isBannerSuccess) StatusSuccess.copy(alpha = 0.15f)
                                else StatusFailed.copy(alpha = 0.15f)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isBannerSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (isBannerSuccess) StatusSuccess else StatusFailed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = bannerText,
                            color = if (isBannerSuccess) StatusSuccess else StatusFailed,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step-by-step Meta Credentials Wizard
            MetaCredentialsWizard(
                state = state,
                onVerifyToken = { token ->
                    viewModel.verifyToken(token)
                },
                onFetchPhoneNumbers = {
                    viewModel.fetchPhoneNumbers()
                },
                onSelectPhoneNumber = { phone ->
                    viewModel.selectPhoneNumber(phone)
                },
                onManualPhoneNumberChange = { id ->
                    val updated = state.credentials.copy(phoneNumberId = id)
                    viewModel.saveCredentials(updated)
                },
                onBusinessIdChange = { id ->
                    viewModel.updateBusinessAccountId(id)
                },
                onWebhookTokenChange = { token ->
                    viewModel.updateWebhookToken(token)
                },
                onGenerateWebhookToken = {
                    viewModel.generateWebhookToken()
                },
                onOpenWebView = {
                    viewModel.showWebViewSheet()
                },
                onSaveAndTest = {
                    viewModel.saveAndTest()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal In-App WebView Bottom Sheet for Meta Graph API Explorer
    if (state.showWebViewSheet) {
        MetaTokenWebViewSheet(
            sheetState = sheetState,
            onTokenConfirmed = { token ->
                viewModel.onTokenCopiedFromWebView(token)
                viewModel.verifyToken(token)
            },
            onDismiss = {
                viewModel.hideWebViewSheet()
            }
        )
    }
}

@Composable
fun PermissionRow(
    title: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BrandTextSecondary.copy(alpha = 0.1f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isGranted) BrandGreen.copy(alpha = 0.2f) else StatusFailed.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = if (isGranted) BrandGreen else StatusFailed,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = BrandTextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (isGranted) "Permission Granted" else "Permission Required",
                color = if (isGranted) StatusSuccess else StatusFailed,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (!isGranted) {
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Grant", fontSize = 12.sp)
            }
        }
    }
}
