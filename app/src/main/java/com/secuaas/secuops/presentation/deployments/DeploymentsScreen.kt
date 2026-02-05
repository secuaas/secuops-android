package com.secuaas.secuops.presentation.deployments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.secuaas.secuops.data.model.Deployment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeploymentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DeploymentsViewModel = hiltViewModel()
) {
    val deploymentsState by viewModel.deploymentsState.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val isLoading = deploymentsState is DeploymentsState.Loading

    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deployments") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (filterStatus != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    IconButton(onClick = { viewModel.loadDeployments() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isLoading),
            onRefresh = { viewModel.loadDeployments() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = deploymentsState) {
                is DeploymentsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is DeploymentsState.Success -> {
                    if (state.deployments.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (filterStatus != null) {
                                        "No deployments with status: $filterStatus"
                                    } else {
                                        "No deployments found"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (filterStatus != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextButton(onClick = { viewModel.setStatusFilter(null) }) {
                                        Text("Clear filter")
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.deployments) { deployment ->
                                DeploymentCard(
                                    deployment = deployment,
                                    onRetry = { viewModel.retryDeployment(deployment.id) }
                                )
                            }
                        }
                    }
                }
                is DeploymentsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadDeployments() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            currentFilter = filterStatus,
            onDismiss = { showFilterDialog = false },
            onFilterSelected = { status ->
                viewModel.setStatusFilter(status)
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun DeploymentCard(
    deployment: Deployment,
    onRetry: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Deployment #${deployment.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = deployment.operation.uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(status = deployment.status)
                    if (deployment.progress > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${deployment.progress}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                if (deployment.commitMessage != null) {
                    InfoRow("Commit", deployment.commitMessage)
                }
                if (deployment.commitSha != null) {
                    InfoRow("SHA", deployment.commitSha.take(8))
                }
                if (deployment.phase != null) {
                    InfoRow("Phase", deployment.phase)
                }
                InfoRow("Started", deployment.startedAt)
                if (deployment.completedAt != null) {
                    InfoRow("Completed", deployment.completedAt)
                }
                if (deployment.duration != null) {
                    InfoRow("Duration", "${deployment.duration}s")
                }
                if (deployment.autoCorrected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text("Auto-corrected") },
                        leadingIcon = {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                if (deployment.errors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Errors (${deployment.errors.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                    deployment.errors.forEach { error ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• ${error.message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (deployment.status == "failed") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry Deployment")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (color, icon) = when (status.lowercase()) {
        "completed" -> MaterialTheme.colorScheme.primary to Icons.Default.CheckCircle
        "deploying", "building" -> MaterialTheme.colorScheme.secondary to Icons.Default.HourglassEmpty
        "failed" -> MaterialTheme.colorScheme.error to Icons.Default.Error
        "pending" -> MaterialTheme.colorScheme.tertiary to Icons.Default.Circle
        else -> MaterialTheme.colorScheme.onSurfaceVariant to Icons.Default.Circle
    }

    AssistChip(
        onClick = {},
        label = { Text(status.uppercase()) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        colors = AssistChipDefaults.assistChipColors(
            leadingIconContentColor = color
        )
    )
}

@Composable
fun FilterDialog(
    currentFilter: String?,
    onDismiss: () -> Unit,
    onFilterSelected: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Status") },
        text = {
            Column {
                val statuses = listOf(null, "pending", "building", "deploying", "completed", "failed")
                statuses.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentFilter == status,
                            onClick = { onFilterSelected(status) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(status?.uppercase() ?: "All")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f)
        )
    }
}
