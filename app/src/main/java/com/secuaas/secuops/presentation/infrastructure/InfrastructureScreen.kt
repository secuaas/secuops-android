package com.secuaas.secuops.presentation.infrastructure

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
import com.secuaas.secuops.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfrastructureScreen(
    onNavigateBack: () -> Unit,
    viewModel: InfrastructureViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pods", "Services", "Ingresses", "Certificates")

    val podsState by viewModel.podsState.collectAsState()
    val servicesState by viewModel.servicesState.collectAsState()
    val ingressesState by viewModel.ingressesState.collectAsState()
    val certificatesState by viewModel.certificatesState.collectAsState()

    val isLoading = when (selectedTab) {
        0 -> podsState is InfrastructureState.Loading
        1 -> servicesState is InfrastructureState.Loading
        2 -> ingressesState is InfrastructureState.Loading
        3 -> certificatesState is InfrastructureState.Loading
        else -> false
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Infrastructure") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.loadAll() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isLoading),
            onRefresh = { viewModel.loadAll() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> PodsTab(podsState)
                1 -> ServicesTab(servicesState)
                2 -> IngressesTab(ingressesState)
                3 -> CertificatesTab(certificatesState)
            }
        }
    }
}

@Composable
fun PodsTab(state: InfrastructureState<List<PodInfo>>) {
    when (state) {
        is InfrastructureState.Loading -> LoadingView()
        is InfrastructureState.Success -> {
            if (state.data.isEmpty()) {
                EmptyView("No pods found")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data) { pod ->
                        PodCard(pod)
                    }
                }
            }
        }
        is InfrastructureState.Error -> ErrorView(state.message)
    }
}

@Composable
fun PodCard(pod: PodInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
                        text = pod.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (pod.status.lowercase()) {
                                "running" -> Icons.Default.CheckCircle
                                "pending" -> Icons.Default.HourglassEmpty
                                "failed", "error" -> Icons.Default.Error
                                else -> Icons.Default.Circle
                            },
                            contentDescription = "Status",
                            modifier = Modifier.size(16.dp),
                            tint = when (pod.status.lowercase()) {
                                "running" -> MaterialTheme.colorScheme.primary
                                "pending" -> MaterialTheme.colorScheme.secondary
                                "failed", "error" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = pod.status.uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                </Column>

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ready: ${pod.ready}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Restarts: ${pod.restarts}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Age: ${pod.age}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun ServicesTab(state: InfrastructureState<List<ServiceInfo>>) {
    when (state) {
        is InfrastructureState.Loading -> LoadingView()
        is InfrastructureState.Success -> {
            if (state.data.isEmpty()) {
                EmptyView("No services found")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data) { service ->
                        ServiceCard(service)
                    }
                }
            }
        }
        is InfrastructureState.Error -> ErrorView(state.message)
    }
}

@Composable
fun ServiceCard(service: ServiceInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = service.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Type", service.type)
            InfoRow("Cluster IP", service.clusterIp)
            InfoRow("Ports", service.ports)
        }
    }
}

@Composable
fun IngressesTab(state: InfrastructureState<List<IngressInfo>>) {
    when (state) {
        is InfrastructureState.Loading -> LoadingView()
        is InfrastructureState.Success -> {
            if (state.data.isEmpty()) {
                EmptyView("No ingresses found")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data) { ingress ->
                        IngressCard(ingress)
                    }
                }
            }
        }
        is InfrastructureState.Error -> ErrorView(state.message)
    }
}

@Composable
fun IngressCard(ingress: IngressInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = ingress.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Hosts", ingress.hosts.joinToString("\n"))
            if (ingress.address != null) {
                InfoRow("Address", ingress.address)
            }
        }
    }
}

@Composable
fun CertificatesTab(state: InfrastructureState<List<CertificateInfo>>) {
    when (state) {
        is InfrastructureState.Loading -> LoadingView()
        is InfrastructureState.Success -> {
            if (state.data.isEmpty()) {
                EmptyView("No certificates found")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data) { cert ->
                        CertificateCard(cert)
                    }
                }
            }
        }
        is InfrastructureState.Error -> ErrorView(state.message)
    }
}

@Composable
fun CertificateCard(cert: CertificateInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (cert.ready) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        )
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
                Text(
                    text = cert.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (cert.ready) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = if (cert.ready) "Ready" else "Not Ready",
                    tint = if (cert.ready) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Secret", cert.secret)
            if (cert.issuer != null) {
                InfoRow("Issuer", cert.issuer)
            }
            if (cert.validUntil != null) {
                InfoRow("Valid Until", cert.validUntil)
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorView(message: String) {
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
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
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
