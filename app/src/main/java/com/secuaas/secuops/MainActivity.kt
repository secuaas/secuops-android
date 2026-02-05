package com.secuaas.secuops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.secuaas.secuops.data.local.TokenManager
import com.secuaas.secuops.presentation.applications.ApplicationsScreen
import com.secuaas.secuops.presentation.auth.LoginScreen
import com.secuaas.secuops.presentation.dashboard.DashboardScreen
import com.secuaas.secuops.presentation.billing.BillingScreen
import com.secuaas.secuops.presentation.deployments.DeploymentsScreen
import com.secuaas.secuops.presentation.domains.DomainsScreen
import com.secuaas.secuops.presentation.infrastructure.InfrastructureScreen
import com.secuaas.secuops.presentation.projects.ProjectsScreen
import com.secuaas.secuops.presentation.servers.ServersScreen
import com.secuaas.secuops.ui.theme.SecuOpsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecuOpsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = if (tokenManager.isLoggedIn()) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Login.route
                    }

                    SecuOpsNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        tokenManager = tokenManager
                    )
                }
            }
        }
    }
}

@Composable
fun SecuOpsNavHost(
    navController: NavHostController,
    startDestination: String,
    tokenManager: TokenManager
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToApplications = { navController.navigate(Screen.Applications.route) },
                onNavigateToProjects = { navController.navigate(Screen.Projects.route) },
                onNavigateToInfrastructure = { navController.navigate(Screen.Infrastructure.route) },
                onNavigateToDomains = { navController.navigate(Screen.Domains.route) },
                onNavigateToServers = { navController.navigate(Screen.Servers.route) },
                onNavigateToBilling = { navController.navigate(Screen.Billing.route) },
                onNavigateToDeployments = { navController.navigate(Screen.Deployments.route) },
                onLogout = {
                    kotlinx.coroutines.runBlocking {
                        tokenManager.clearAll()
                    }
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Applications.route) {
            ApplicationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Projects.route) {
            ProjectsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Infrastructure.route) {
            InfrastructureScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Deployments.route) {
            DeploymentsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Domains.route) {
            DomainsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Servers.route) {
            ServersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Billing.route) {
            BillingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Applications : Screen("applications")
    object Projects : Screen("projects")
    object Infrastructure : Screen("infrastructure")
    object Domains : Screen("domains")
    object Servers : Screen("servers")
    object Billing : Screen("billing")
    object Deployments : Screen("deployments")
}
