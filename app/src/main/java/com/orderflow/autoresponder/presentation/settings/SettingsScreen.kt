package com.orderflow.autoresponder.presentation.settings

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReplyAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkNotificationAccess()
    }

    var phoneNumberId by remember(state.credentials) { mutableStateOf(state.credentials.phoneNumberId) }
    var accessToken by remember(state.credentials) { mutableStateOf(state.credentials.accessToken) }
    var businessAccountId by remember(state.credentials) { mutableStateOf(state.credentials.businessAccountId) }
    var webhookVerifyToken by remember(state.credentials) { mutableStateOf(state.credentials.webhookVerifyToken) }

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
                text = "Meta API Credentials",
                style = MaterialTheme.typography.titleLarge,
                color = BrandTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneNumberId,
                onValueChange = { phoneNumberId = it },
                label = { Text("Phone Number ID") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = accessToken,
                onValueChange = { accessToken = it },
                label = { Text("Permanent Access Token") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = businessAccountId,
                onValueChange = { businessAccountId = it },
                label = { Text("Business Account ID") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val newCreds = MetaCredentials(
                        phoneNumberId = phoneNumberId,
                        accessToken = accessToken,
                        businessAccountId = businessAccountId,
                        webhookVerifyToken = webhookVerifyToken
                    )
                    viewModel.saveCredentials(newCreds)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Save Configuration", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val currentCreds = MetaCredentials(
                        phoneNumberId = phoneNumberId,
                        accessToken = accessToken,
                        businessAccountId = businessAccountId,
                        webhookVerifyToken = webhookVerifyToken
                    )
                    viewModel.testApiConnection(currentCreds)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = !state.isTestingConnection
            ) {
                if (state.isTestingConnection) {
                    CircularProgressIndicator(color = BrandAccent)
                } else {
                    Text(text = "Test API Connection", color = BrandAccent)
                }
            }
        }
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

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandAccent,
    unfocusedBorderColor = BrandTextSecondary,
    focusedLabelColor = BrandAccent,
    unfocusedLabelColor = BrandTextSecondary,
    focusedTextColor = BrandTextPrimary,
    unfocusedTextColor = BrandTextPrimary,
    cursorColor = BrandAccent
)
