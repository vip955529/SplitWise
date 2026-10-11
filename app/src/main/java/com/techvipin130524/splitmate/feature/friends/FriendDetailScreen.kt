package com.techvipin130524.splitmate.feature.friends

import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techvipin130524.splitmate.core.designsystem.component.EmptyState
import com.techvipin130524.splitmate.core.designsystem.component.ExpenseRow
import com.techvipin130524.splitmate.core.designsystem.component.PersonAvatar
import com.techvipin130524.splitmate.core.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friendId: String,
    onBackClick: () -> Unit,
    viewModel: FriendDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(friendId) {
        viewModel.loadFriendDetails(friendId)
    }

    val friend = uiState.friend

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(friend?.friendName ?: "Friend Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (friend != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PersonAvatar(name = friend.friendName, size = 48)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friend.friendName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = friend.friendEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val netBal = uiState.friendNetBalancePaise
                            Text(
                                text = when {
                                    netBal > 0 -> "${friend.friendName} owes you ${CurrencyUtils.formatPaise(netBal)}"
                                    netBal < 0 -> "You owe ${friend.friendName} ${CurrencyUtils.formatPaise(-netBal)}"
                                    else -> "Settled up with ${friend.friendName}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    netBal > 0 -> Color(0xFF2E7D32)
                                    netBal < 0 -> Color(0xFFC62828)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Shared Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.sharedExpenses.isEmpty()) {
                    EmptyState(
                        title = "No Shared Expenses",
                        subtitle = "Expenses shared with ${friend.friendName} will appear here.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.sharedExpenses, key = { it.id }) { expense ->
                            val myShare = expense.participants.find { it.userId == uiState.currentUserId }?.amount ?: 0L
                            val isPayer = expense.createdBy == uiState.currentUserId || expense.payers.any { it.userId == uiState.currentUserId }

                            ExpenseRow(
                                description = expense.description,
                                createdByName = expense.createdByName,
                                totalAmountPaise = expense.totalAmount,
                                userSharePaise = myShare,
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
    }
}
