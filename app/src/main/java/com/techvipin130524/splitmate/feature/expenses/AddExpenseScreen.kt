package com.techvipin130524.splitmate.feature.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techvipin130524.splitmate.domain.model.ExpenseCategory
import com.techvipin130524.splitmate.domain.model.SplitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    initialGroupId: String?,
    onBackClick: () -> Unit,
    onExpenseSaved: () -> Unit,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var groupDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(initialGroupId) {
        if (!initialGroupId.isNullOrEmpty()) {
            viewModel.selectGroup(initialGroupId)
        }
    }

    LaunchedEffect(uiState.error, uiState.message) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Group Dropdown Selector
            ExposedDropdownMenuBox(
                expanded = groupDropdownExpanded,
                onExpandedChange = { groupDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.selectedGroup?.name ?: "Select Group",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = groupDropdownExpanded,
                    onDismissRequest = { groupDropdownExpanded = false }
                ) {
                    uiState.groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name) },
                            onClick = {
                                viewModel.selectGroup(group.id)
                                groupDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Expense Description (e.g. Dinner, Rent)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.totalAmountString,
                onValueChange = viewModel::updateTotalAmount,
                label = { Text("Total Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text("Note / Comment (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.receiptImageUrl,
                onValueChange = viewModel::updateReceiptImageUrl,
                label = { Text("Receipt Image URL (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.category.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    ExpenseCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                viewModel.updateCategory(category)
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Paid By / Multiple Payers Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isMultiplePayers) "Multiple Payers" else "Single Payer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text("Multiple Payers", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = uiState.isMultiplePayers,
                    onCheckedChange = viewModel::toggleMultiplePayers
                )
            }

            if (uiState.isMultiplePayers) {
                val members = uiState.selectedGroup?.members ?: emptyList()
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Enter amount paid by each person:", style = MaterialTheme.typography.labelSmall)
                    members.forEach { m ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(m.userName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = uiState.payerAmounts[m.userId] ?: "",
                                onValueChange = { viewModel.updatePayerAmount(m.userId, it) },
                                label = { Text("Paid ₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Split Type Tabs
            Text("Split Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            val splitTypes = SplitType.entries.toTypedArray()
            val selectedIndex = splitTypes.indexOf(uiState.splitType)

            TabRow(selectedTabIndex = selectedIndex) {
                splitTypes.forEachIndexed { index, type ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { viewModel.updateSplitType(type) },
                        text = { Text(type.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Paid By / Multiple Payers Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isMultiplePayers) "Multiple Payers" else "Single Payer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text("Multiple Payers", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = uiState.isMultiplePayers,
                    onCheckedChange = viewModel::toggleMultiplePayers
                )
            }

            if (uiState.isMultiplePayers) {
                val members = uiState.selectedGroup?.members ?: emptyList()
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Enter amount paid by each person:", style = MaterialTheme.typography.labelSmall)
                    members.forEach { m ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(m.userName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = uiState.payerAmounts[m.userId] ?: "",
                                onValueChange = { viewModel.updatePayerAmount(m.userId, it) },
                                label = { Text("Paid ₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Participants", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            val members = uiState.selectedGroup?.members ?: emptyList()

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(members, key = { it.userId }) { member ->
                    val isSelected = member.userId in uiState.selectedParticipantIds

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleParticipant(member.userId) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleParticipant(member.userId) }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = member.userName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            when (uiState.splitType) {
                                SplitType.EXACT -> {
                                    OutlinedTextField(
                                        value = uiState.exactAmounts[member.userId] ?: "",
                                        onValueChange = { viewModel.updateExactAmount(member.userId, it) },
                                        label = { Text("Amount ₹") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.width(100.dp)
                                    )
                                }
                                SplitType.PERCENTAGE -> {
                                    OutlinedTextField(
                                        value = uiState.percentages[member.userId] ?: "",
                                        onValueChange = { viewModel.updatePercentage(member.userId, it) },
                                        label = { Text("%") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.width(80.dp)
                                    )
                                }
                                SplitType.SHARES -> {
                                    OutlinedTextField(
                                        value = uiState.shares[member.userId] ?: "1",
                                        onValueChange = { viewModel.updateShares(member.userId, it) },
                                        label = { Text("Shares") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.width(80.dp)
                                    )
                                }
                                SplitType.EQUAL -> {
                                    Text(
                                        text = "Equal share",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Paid By / Multiple Payers Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isMultiplePayers) "Multiple Payers" else "Single Payer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text("Multiple Payers", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = uiState.isMultiplePayers,
                    onCheckedChange = viewModel::toggleMultiplePayers
                )
            }

            if (uiState.isMultiplePayers) {
                val members = uiState.selectedGroup?.members ?: emptyList()
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Enter amount paid by each person:", style = MaterialTheme.typography.labelSmall)
                    members.forEach { m ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(m.userName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = uiState.payerAmounts[m.userId] ?: "",
                                onValueChange = { viewModel.updatePayerAmount(m.userId, it) },
                                label = { Text("Paid ₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveExpense(onExpenseSaved) },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Expense")
            }
        }
    }
}
