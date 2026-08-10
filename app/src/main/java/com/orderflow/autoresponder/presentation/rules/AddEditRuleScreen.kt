package com.orderflow.autoresponder.presentation.rules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orderflow.autoresponder.domain.model.AutoReplyRule
import com.orderflow.autoresponder.domain.model.MatchOption
import com.orderflow.autoresponder.presentation.components.AppTopBar
import com.orderflow.autoresponder.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleScreen(
    editingRule: AutoReplyRule? = null,
    viewModel: RulesViewModel,
    onNavigateBack: () -> Unit
) {
    var ruleName by remember { mutableStateOf(editingRule?.ruleName ?: "") }
    var keywordsCsv by remember { mutableStateOf(editingRule?.keywordsCsv ?: "") }
    var replyMessage by remember { mutableStateOf(editingRule?.replyMessagesJson ?: "") }
    var selectedMatchOption by remember { mutableStateOf(editingRule?.matchOption ?: MatchOption.EXACT) }
    var delaySecondsText by remember { mutableStateOf(editingRule?.delaySeconds?.toString() ?: "0") }
    var replySequential by remember { mutableStateOf(editingRule?.replySequential ?: false) }

    var expandedDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (editingRule == null) "Create New Rule" else "Edit Rule",
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
            OutlinedTextField(
                value = ruleName,
                onValueChange = { ruleName = it },
                label = { Text("Rule Name") },
                placeholder = { Text("e.g. Welcome Message") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = !expandedDropdown }
            ) {
                OutlinedTextField(
                    value = selectedMatchOption.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Matching Option") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    MatchOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option.name) },
                            onClick = {
                                selectedMatchOption = option
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = keywordsCsv,
                onValueChange = { keywordsCsv = it },
                label = { Text("Keywords (comma separated)") },
                placeholder = { Text("e.g. price, cost, rate") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = replyMessage,
                onValueChange = { replyMessage = it },
                label = { Text("Automated Reply Message") },
                placeholder = { Text("Hi %name%! Separate messages with || for sequential sending.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = replySequential,
                    onCheckedChange = { replySequential = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = BrandGreen,
                        uncheckedColor = BrandTextSecondary
                    )
                )
                Text(
                    text = "Send Multiple Messages Sequentially",
                    color = BrandTextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = delaySecondsText,
                onValueChange = { delaySecondsText = it },
                label = { Text("Reply Delay (seconds between messages)") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val delay = delaySecondsText.toIntOrNull() ?: 0
                    viewModel.saveRule(
                        id = editingRule?.id ?: 0L,
                        ruleName = ruleName,
                        keywordsCsv = keywordsCsv,
                        replyMessage = replyMessage,
                        matchOption = selectedMatchOption,
                        delaySeconds = delay,
                        replySequential = replySequential
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (editingRule == null) "Save Rule" else "Update Rule",
                    style = MaterialTheme.typography.titleLarge
                )
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
