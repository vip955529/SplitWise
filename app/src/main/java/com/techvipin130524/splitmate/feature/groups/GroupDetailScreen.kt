package com.techvipin130524.splitmate.feature.groups

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import com.techvipin130524.splitmate.core.designsystem.component.EmptyState
import com.techvipin130524.splitmate.domain.model.ExpenseCategory
import com.techvipin130524.splitmate.core.designsystem.component.ExpenseRow
import com.techvipin130524.splitmate.core.designsystem.component.PersonAvatar
import com.techvipin130524.splitmate.core.utils.CurrencyUtils

import com.techvipin130524.splitmate.domain.model.Settlement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBackClick: () -> Unit,
    onAddExpenseClick: (String) -> Unit,
    onSettleUpClick: (String) -> Unit,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCategoryFilter by remember { mutableStateOf<ExpenseCategory?>(null) }
    var dateFilter by remember { mutableStateOf("ALL") }
    var selectedSettlementForDetails by remember { mutableStateOf<Settlement?>(null) }
    var memberSearchQuery by remember { mutableStateOf("") }
    var activitySearchQuery by remember { mutableStateOf("") }
    val tabs = listOf("Expenses", "Balances", "Members", "Activity", "Analytics")

    LaunchedEffect(groupId) {
        viewModel.selectGroup(groupId)
    }

    val group = uiState.selectedGroup

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Group Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            try {
                                val csvHeader = "Expense ID,Description,Category,Total Amount (Paise),Paid By,Created At\n"
                                val csvRows = uiState.groupExpenses.joinToString("\n") { e ->
                                    "\"${e.id}\",\"${e.description}\",\"${e.category.displayName}\",${e.totalAmount},\"${e.createdByName}\",\"${e.createdAt}\""
                                }
                                val csvContent = csvHeader + csvRows
                                val cacheFile = File(context.cacheDir, "${group?.name ?: "Group"}_Expenses.csv")
                                cacheFile.writeText(csvContent)

                                val contentUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    cacheFile
                                )

                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "${group?.name} CSV Report")
                                    putExtra(Intent.EXTRA_STREAM, contentUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share CSV Report"))
                            } catch (e: Exception) {
                                // Fallback to plain text share if FileProvider not configured
                                val report = buildString {
                                    appendLine("📊 ${group?.name ?: "Group"} Expense Summary")
                                    uiState.groupExpenses.forEach { exp ->
                                        appendLine("• ${exp.description}: ${CurrencyUtils.formatPaise(exp.totalAmount)} (Paid by ${exp.createdByName})")
                                    }
                                }
                                val textIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, report)
                                }
                                context.startActivity(Intent.createChooser(textIntent, "Share Summary"))
                            }
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV")
                    }

                    IconButton(
                        onClick = {
                            val report = buildString {
                                appendLine("📊 ${group?.name ?: "Group"} Expense Report")
                                appendLine("Members: ${group?.members?.size ?: 0}")
                                appendLine("------------------------------")
                                appendLine("Outstanding Balances:")
                                if (uiState.groupSimplifiedDebts.isEmpty()) {
                                    appendLine("All settled up!")
                                } else {
                                    uiState.groupSimplifiedDebts.forEach { debt ->
                                        appendLine("• ${debt.fromUserName} owes ${debt.toUserName}: ${CurrencyUtils.formatPaise(debt.amount)}")
                                    }
                                }
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "${group?.name} Expense Report")
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Expense Report"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report")
                    }

                    var topMenuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { topMenuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = topMenuExpanded,
                        onDismissRequest = { topMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Leave Group", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                topMenuExpanded = false
                                viewModel.leaveGroup(groupId) {
                                    onBackClick()
                                }
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddExpenseClick(groupId) }) {
                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Expense")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!group?.inviteCode.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Group Invite Code",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = group?.inviteCode ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    // Expenses Tab
                    val now = System.currentTimeMillis()
                    val oneMonthAgo = now - 30L * 24 * 60 * 60 * 1000
                    val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000

                    val dateFilteredExpenses = uiState.groupExpenses.filter { e ->
                        when (dateFilter) {
                            "THIS_MONTH" -> e.createdAt >= oneMonthAgo
                            "LAST_7_DAYS" -> e.createdAt >= sevenDaysAgo
                            else -> true
                        }
                    }

                    val filteredExpenses = if (selectedCategoryFilter == null) {
                        dateFilteredExpenses
                    } else {
                        dateFilteredExpenses.filter { it.category == selectedCategoryFilter }
                    }

                    val totalGroupSpend = uiState.groupExpenses.sumOf { it.totalAmount }
                    val userPaidInGroup = uiState.groupExpenses.filter { it.createdBy == uiState.currentUserId || it.payers.any { p -> p.userId == uiState.currentUserId } }
                        .sumOf { exp -> exp.payers.find { it.userId == uiState.currentUserId }?.amount ?: exp.totalAmount }
                    val userShareInGroup = uiState.groupExpenses.sumOf { exp -> exp.participants.find { it.userId == uiState.currentUserId }?.amount ?: 0L }
                    val myNetInGroup = userPaidInGroup - userShareInGroup

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Group Summary Header Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Group Spend", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(CurrencyUtils.formatPaise(totalGroupSpend), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Your Net Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        CurrencyUtils.formatSignedPaise(myNetInGroup),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (myNetInGroup >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                            }
                        }

                        // Category & Date Filter Chips Row
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = dateFilter == "ALL",
                                    onClick = { dateFilter = "ALL" },
                                    label = { Text("All Time") },
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                            item {
                                FilterChip(
                                    selected = dateFilter == "THIS_MONTH",
                                    onClick = { dateFilter = "THIS_MONTH" },
                                    label = { Text("This Month") },
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                            item {
                                FilterChip(
                                    selected = dateFilter == "LAST_7_DAYS",
                                    onClick = { dateFilter = "LAST_7_DAYS" },
                                    label = { Text("Last 7 Days") },
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == null,
                                    onClick = { selectedCategoryFilter = null },
                                    label = { Text("All Categories") },
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                            items(ExpenseCategory.entries) { cat ->
                                FilterChip(
                                    selected = selectedCategoryFilter == cat,
                                    onClick = {
                                        selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                    },
                                    label = { Text(cat.displayName) },
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                        }

                        if (filteredExpenses.isEmpty()) {
                            EmptyState(
                                title = if (selectedCategoryFilter != null) "No ${selectedCategoryFilter?.displayName} Expenses" else "No Expenses Yet",
                                subtitle = "Tap 'Add Expense' to record a shared cost in this group.",
                                buttonText = "Add Expense",
                                onButtonClick = { onAddExpenseClick(groupId) },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                items(filteredExpenses, key = { it.id }) { expense ->
                                    val userShare = expense.participants.find { it.userId == uiState.currentUserId }?.amount ?: 0L
                                    val isPayer = expense.createdBy == uiState.currentUserId || expense.payers.any { it.userId == uiState.currentUserId }

                                    ExpenseRow(
                                        description = expense.description,
                                        createdByName = expense.createdByName,
                                        totalAmountPaise = expense.totalAmount,
                                        userSharePaise = userShare,
                                        isPayer = isPayer,
                                        category = expense.category,
                                        onClick = {},
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Balances Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Simplified Debt Settlements",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = { onSettleUpClick(groupId) }
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Settle Up")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.groupSimplifiedDebts.isEmpty()) {
                            Text(
                                text = "All settled up! No outstanding debts in this group.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            LazyColumn {
                                items(uiState.groupSimplifiedDebts) { debt ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedSettlementForDetails = Settlement(
                                                    groupId = groupId,
                                                    fromUserId = debt.fromUserId,
                                                    fromUserName = debt.fromUserName,
                                                    toUserId = debt.toUserId,
                                                    toUserName = debt.toUserName,
                                                    amount = debt.amount,
                                                    note = "Calculated via Greedy Debt Simplification Engine"
                                                )
                                            }
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${debt.fromUserName} owes ${debt.toUserName}",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = CurrencyUtils.formatPaise(debt.amount),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC62828)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Members Tab
                    val currentMemberRole = group?.members?.find { it.userId == uiState.currentUserId }?.role ?: "MEMBER"
                    val isAdmin = currentMemberRole == "ADMIN" || group?.createdBy == uiState.currentUserId

                    val allMembers = group?.members ?: emptyList()
                    val filteredMembers = if (memberSearchQuery.isBlank()) {
                        allMembers
                    } else {
                        val q = memberSearchQuery.trim().lowercase()
                        allMembers.filter { it.userName.lowercase().contains(q) || it.userEmail.lowercase().contains(q) }
                    }

                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        OutlinedTextField(
                            value = memberSearchQuery,
                            onValueChange = { memberSearchQuery = it },
                            label = { Text("Search members...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(filteredMembers) { member ->
                                var memberMenuExpanded by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        PersonAvatar(name = member.userName, size = 40)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = member.userName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = member.userEmail,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = member.role,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (member.role == "ADMIN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )

                                        if (isAdmin && member.userId != uiState.currentUserId) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box {
                                                IconButton(onClick = { memberMenuExpanded = true }) {
                                                    Icon(Icons.Default.MoreVert, contentDescription = "Member Actions")
                                                }
                                                DropdownMenu(
                                                    expanded = memberMenuExpanded,
                                                    onDismissRequest = { memberMenuExpanded = false }
                                                ) {
                                                    if (member.role == "ADMIN") {
                                                        DropdownMenuItem(
                                                            text = { Text("Make Member") },
                                                            onClick = {
                                                                memberMenuExpanded = false
                                                                viewModel.updateMemberRole(groupId, member.userId, "MEMBER")
                                                            }
                                                        )
                                                    } else {
                                                        DropdownMenuItem(
                                                            text = { Text("Make Admin") },
                                                            onClick = {
                                                                memberMenuExpanded = false
                                                                viewModel.updateMemberRole(groupId, member.userId, "ADMIN")
                                                            }
                                                        )
                                                    }
                                                    DropdownMenuItem(
                                                        text = { Text("Remove from Group", color = MaterialTheme.colorScheme.error) },
                                                        onClick = {
                                                            memberMenuExpanded = false
                                                            viewModel.removeMember(groupId, member.userId)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Activity Feed Tab
                    val allLogs = uiState.groupActivityLogs
                    val filteredLogs = if (activitySearchQuery.isBlank()) {
                        allLogs
                    } else {
                        val q = activitySearchQuery.trim().lowercase()
                        allLogs.filter { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = activitySearchQuery,
                            onValueChange = { activitySearchQuery = it },
                            label = { Text("Search activity logs...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (filteredLogs.isEmpty()) {
                            EmptyState(
                                title = "No Activity Found",
                                subtitle = "Expenses and settlements recorded in this group will appear here.",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(filteredLogs, key = { it.id }) { log ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = log.title,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = log.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // Analytics Tab
                    val groupExpenses = uiState.groupExpenses
                    val totalGroupSpend = groupExpenses.sumOf { it.totalAmount }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Total Group Spending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyUtils.formatPaise(totalGroupSpend),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Member Contributions Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val members = group?.members ?: emptyList()
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Column {
                                    members.forEach { m ->
                                        val paidByMember = groupExpenses.filter { it.createdBy == m.userId || it.payers.any { p -> p.userId == m.userId } }
                                            .sumOf { exp -> exp.payers.find { it.userId == m.userId }?.amount ?: exp.totalAmount }

                                        val fraction = if (totalGroupSpend > 0) (paidByMember.toFloat() / totalGroupSpend).coerceIn(0f, 1f) else 0f

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(m.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                    Text(CurrencyUtils.formatPaise(paidByMember), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                LinearProgressIndicator(
                                                    progress = { fraction },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Text(
                                        text = "Group Category Spending Breakdown",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpenseCategory.entries.forEach { cat ->
                                        val catSpend = groupExpenses.filter { it.category == cat }.sumOf { it.totalAmount }
                                        if (catSpend > 0) {
                                            val catFraction = if (totalGroupSpend > 0) (catSpend.toFloat() / totalGroupSpend).coerceIn(0f, 1f) else 0f
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(cat.displayName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                                        Text(CurrencyUtils.formatPaise(catSpend), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    LinearProgressIndicator(
                                                        progress = { catFraction },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(6.dp)
                                                            .clip(RoundedCornerShape(3.dp)),
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedSettlementForDetails?.let { settlement ->
        AlertDialog(
            onDismissRequest = { selectedSettlementForDetails = null },
            title = { Text("Settlement Details") },
            text = {
                Column {
                    Text("Payer: ${settlement.fromUserName}", fontWeight = FontWeight.Bold)
                    Text("Recipient: ${settlement.toUserName}", fontWeight = FontWeight.Bold)
                    Text("Amount: ${CurrencyUtils.formatPaise(settlement.amount)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Note: ${settlement.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = { selectedSettlementForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}
