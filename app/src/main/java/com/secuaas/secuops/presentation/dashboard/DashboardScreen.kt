package com.secuaas.secuops.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToApplications: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToInfrastructure: () -> Unit,
    onNavigateToDomains: () -> Unit,
    onNavigateToServers: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToDeployments: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SecuOps Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Welcome Section
            Text(
                text = "Infrastructure Management",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Dashboard Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DashboardCard(
                        title = "Applications",
                        icon = Icons.Default.Apps,
                        onClick = onNavigateToApplications
                    )
                }
                item {
                    DashboardCard(
                        title = "Deployments",
                        icon = Icons.Default.CloudUpload,
                        onClick = onNavigateToDeployments
                    )
                }
                item {
                    DashboardCard(
                        title = "Projects",
                        icon = Icons.Default.Folder,
                        onClick = onNavigateToProjects
                    )
                }
                item {
                    DashboardCard(
                        title = "Infrastructure",
                        icon = Icons.Default.Cloud,
                        onClick = onNavigateToInfrastructure
                    )
                }
                item {
                    DashboardCard(
                        title = "Domains",
                        icon = Icons.Default.Language,
                        onClick = onNavigateToDomains
                    )
                }
                item {
                    DashboardCard(
                        title = "Servers/VPS",
                        icon = Icons.Default.Storage,
                        onClick = onNavigateToServers
                    )
                }
                item {
                    DashboardCard(
                        title = "Billing",
                        icon = Icons.Default.Receipt,
                        onClick = onNavigateToBilling
                    )
                }
                item {
                    DashboardCard(
                        title = "Settings",
                        icon = Icons.Default.Settings,
                        onClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
