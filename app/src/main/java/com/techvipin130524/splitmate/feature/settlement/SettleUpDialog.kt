package com.techvipin130524.splitmate.feature.settlement

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpDialog(
    groupId: String,
    onDismiss: () -> Unit,
    onSettled: () -> Unit,
    viewModel: SettlementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var upiIdInput by remember { mutableStateOf("") }
    var fromDropdownExpanded by remember { mutableStateOf(false) }
    var toDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(groupId) {
        viewModel.loadGroup(groupId)
    }

    val members = uiState.group?.members ?: emptyList()
    val fromUser = members.find { it.userId == uiState.fromUserId }
    val toUser = members.find { it.userId == uiState.toUserId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settle Up") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // From User Dropdown
                Text("Who paid?")
                ExposedDropdownMenuBox(
                    expanded = fromDropdownExpanded,
                    onExpandedChange = { fromDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = fromUser?.userName ?: "Select Payer",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fromDropdownExpanded,
                        onDismissRequest = { fromDropdownExpanded = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.userName) },
                                onClick = {
                                    viewModel.updateFromUser(member.userId)
                                    fromDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // To User Dropdown
                Text("Who received?")
                ExposedDropdownMenuBox(
                    expanded = toDropdownExpanded,
                    onExpandedChange = { toDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = toUser?.userName ?: "Select Recipient",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = toDropdownExpanded,
                        onDismissRequest = { toDropdownExpanded = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.userName) },
                                onClick = {
                                    viewModel.updateToUser(member.userId)
                                    toDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.amountString,
                    onValueChange = viewModel::updateAmount,
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::updateNote,
                    label = { Text("Note (e.g. Paid via UPI)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = upiIdInput,
                    onValueChange = { upiIdInput = it },
                    label = { Text("Recipient UPI ID (Optional)") },
                    placeholder = { Text("e.g. name@upi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (upiIdInput.isNotBlank() && uiState.amountString.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val uri = Uri.parse("upi://pay?pa=${upiIdInput.trim()}&pn=${toUser?.userName ?: "Recipient"}&am=${uiState.amountString.trim()}&cu=INR")
                            val upiIntent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(Intent.createChooser(upiIntent, "Pay via UPI App"))
                            } catch (e: Exception) {
                                // Handled if no UPI app installed
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pay via UPI App 💸")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.recordSettlement {
                        onSettled()
                        onDismiss()
                    }
                },
                enabled = uiState.amountString.isNotBlank() && !uiState.isSaving
            ) {
                Text("Save Settlement")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
