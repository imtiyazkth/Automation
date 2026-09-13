package com.personalai.os

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.personalai.os.ui.Screen
import com.personalai.os.ui.agentcenter.AgentCenterScreen
import com.personalai.os.ui.agentcenter.AgentCenterViewModel
import com.personalai.os.ui.approvals.ApprovalScreen
import com.personalai.os.ui.approvals.ApprovalViewModel
import com.personalai.os.ui.audit.AuditLogScreen
import com.personalai.os.ui.audit.AuditLogViewModel
import com.personalai.os.ui.chat.ChatViewModel
import com.personalai.os.ui.chat.HeadAgentChatScreen
import com.personalai.os.ui.dashboard.DashboardScreen
import com.personalai.os.ui.dashboard.DashboardViewModel
import com.personalai.os.ui.permissioncenter.PermissionCenterScreen
import com.personalai.os.ui.permissioncenter.PermissionCenterViewModel
import com.personalai.os.ui.theme.AutomationOsTheme

private fun <T : ViewModel> simpleFactory(create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as AutomationOsApp

        setContent {
            AutomationOsTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route ?: Screen.Chat.route
                val currentTitle = Screen.bottomBarScreens.firstOrNull { it.route == currentRoute }?.label
                    ?: "Personal AI Automation OS"

                Scaffold(
                    topBar = { TopAppBar(title = { Text(currentTitle) }) },
                    bottomBar = {
                        NavigationBar {
                            val currentDestination = backStackEntry?.destination
                            Screen.bottomBarScreens.forEach { screen ->
                                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Text(screen.emoji) },
                                    label = { Text(screen.label) }
                                )
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Chat.route,
                        modifier = Modifier.padding(padding)
                    ) {
                        composable(Screen.Chat.route) {
                            val vm: ChatViewModel = viewModel(factory = simpleFactory { ChatViewModel(app.headAgent) })
                            HeadAgentChatScreen(vm)
                        }
                        composable(Screen.Dashboard.route) {
                            val vm: DashboardViewModel = viewModel(factory = simpleFactory {
                                DashboardViewModel(
                                    app.database.messageDao(), app.database.crmDao(),
                                    app.approvalManager, app.auditLogger, app.modeStore
                                )
                            })
                            DashboardScreen(vm)
                        }
                        composable(Screen.Approvals.route) {
                            val vm: ApprovalViewModel = viewModel(factory = simpleFactory {
                                ApprovalViewModel(app.approvalManager, app.headAgent)
                            })
                            ApprovalScreen(vm)
                        }
                        composable(Screen.Agents.route) {
                            val vm: AgentCenterViewModel = viewModel(factory = simpleFactory {
                                AgentCenterViewModel(app.agentRegistry, app.modeStore, app.permissionManager)
                            })
                            AgentCenterScreen(vm)
                        }
                        composable(Screen.Permissions.route) {
                            val vm: PermissionCenterViewModel = viewModel(factory = simpleFactory {
                                PermissionCenterViewModel(app.agentRegistry, app.permissionManager)
                            })
                            PermissionCenterScreen(vm)
                        }
                        composable(Screen.Audit.route) {
                            val vm: AuditLogViewModel = viewModel(factory = simpleFactory {
                                AuditLogViewModel(app.auditLogger)
                            })
                            AuditLogScreen(vm)
                        }
                    }
                }
            }
        }
    }
}
