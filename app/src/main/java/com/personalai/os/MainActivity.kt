package com.personalai.os

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
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
import com.personalai.os.ui.bottomBarScreens
import com.personalai.os.ui.chat.ChatViewModel
import com.personalai.os.ui.chat.HeadAgentChatScreen
import com.personalai.os.ui.components.tappable
import com.personalai.os.ui.dashboard.DashboardScreen
import com.personalai.os.ui.dashboard.DashboardViewModel
import com.personalai.os.ui.diagnostics.DiagnosticsScreen
import com.personalai.os.ui.diagnostics.DiagnosticsViewModel
import com.personalai.os.ui.more.MoreScreen
import com.personalai.os.ui.moreScreens
import com.personalai.os.ui.permissioncenter.PermissionCenterScreen
import com.personalai.os.ui.permissioncenter.PermissionCenterViewModel
import com.personalai.os.ui.scheduled.ScheduledTasksScreen
import com.personalai.os.ui.scheduled.ScheduledTasksViewModel
import com.personalai.os.ui.theme.AppTheme
import com.personalai.os.ui.theme.AutomationOsTheme

private fun <T : ViewModel> simpleFactory(create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }

private fun isDetail(route: String?): Boolean = moreScreens.any { it.route == route }

private fun isTabSelected(screen: Screen, currentRoute: String): Boolean =
    screen.route == currentRoute || (screen == Screen.More && isDetail(currentRoute))

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AutomationOsApp

        setContent {
            AutomationOsTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route ?: Screen.Chat.route
                val pendingApprovals = remember(currentRoute) { app.approvalManager.all().size }

                fun openTab(screen: Screen) {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                Scaffold(
                    modifier = Modifier.imePadding(),
                    containerColor = AppTheme.colors.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        AppTabBar(
                            currentRoute = currentRoute,
                            pendingApprovals = pendingApprovals,
                            onSelect = { openTab(it) }
                        )
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Chat.route,
                        modifier = Modifier.padding(padding),
                        // Tabs cross-fade. Screens opened from More slide in and out along the same axis.
                        enterTransition = {
                            if (isDetail(targetState.destination.route)) slideInHorizontally(tween(260)) { it / 4 } + fadeIn(tween(200))
                            else fadeIn(tween(150))
                        },
                        exitTransition = {
                            if (isDetail(targetState.destination.route)) slideOutHorizontally(tween(260)) { -it / 6 } + fadeOut(tween(200))
                            else fadeOut(tween(100))
                        },
                        popEnterTransition = {
                            if (isDetail(initialState.destination.route)) slideInHorizontally(tween(260)) { -it / 6 } + fadeIn(tween(200))
                            else fadeIn(tween(150))
                        },
                        popExitTransition = {
                            if (isDetail(initialState.destination.route)) slideOutHorizontally(tween(260)) { it / 4 } + fadeOut(tween(200))
                            else fadeOut(tween(100))
                        }
                    ) {
                        composable(Screen.Chat.route) {
                            val vm: ChatViewModel = viewModel(factory = simpleFactory { ChatViewModel(app.headAgent, app.approvalManager, app) })
                            HeadAgentChatScreen(vm)
                        }
                        composable(Screen.Dashboard.route) {
                            val vm: DashboardViewModel = viewModel(factory = simpleFactory {
                                DashboardViewModel(
                                    app.database.messageDao(), app.database.crmDao(),
                                    app.approvalManager, app.auditLogger, app.modeStore
                                )
                            })
                            DashboardScreen(vm, onOpenApprovals = { openTab(Screen.Approvals) })
                        }
                        composable(Screen.Approvals.route) {
                            val vm: ApprovalViewModel = viewModel(factory = simpleFactory {
                                ApprovalViewModel(app.approvalManager, app.headAgent)
                            })
                            ApprovalScreen(vm, onOpenChat = { openTab(Screen.Chat) })
                        }
                        composable(Screen.Agents.route) {
                            val vm: AgentCenterViewModel = viewModel(factory = simpleFactory {
                                AgentCenterViewModel(app.agentRegistry, app.modeStore, app.permissionManager)
                            })
                            AgentCenterScreen(vm)
                        }
                        composable(Screen.More.route) {
                            MoreScreen(onOpen = { screen ->
                                navController.navigate(screen.route) { launchSingleTop = true }
                            })
                        }
                        composable(Screen.Scheduled.route) {
                            val vm: ScheduledTasksViewModel = viewModel(factory = simpleFactory {
                                ScheduledTasksViewModel(app.database.scheduledTaskDao(), app)
                            })
                            ScheduledTasksScreen(vm, onBack = { navController.popBackStack() })
                        }
                        composable(Screen.Permissions.route) {
                            val vm: PermissionCenterViewModel = viewModel(factory = simpleFactory {
                                PermissionCenterViewModel(app.agentRegistry, app.permissionManager)
                            })
                            PermissionCenterScreen(
                                vm,
                                onBack = { navController.popBackStack() },
                                onOpenAgents = { openTab(Screen.Agents) }
                            )
                        }
                        composable(Screen.Audit.route) {
                            val vm: AuditLogViewModel = viewModel(factory = simpleFactory {
                                AuditLogViewModel(app.auditLogger)
                            })
                            AuditLogScreen(
                                vm,
                                onBack = { navController.popBackStack() },
                                onOpenChat = { openTab(Screen.Chat) }
                            )
                        }
                        composable(Screen.Diagnostics.route) {
                            val vm: DiagnosticsViewModel = viewModel(factory = simpleFactory { DiagnosticsViewModel() })
                            DiagnosticsScreen(vm, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

/**
 * Five tabs, icon above a short label, no selection pill. The selected tab takes the accent colour.
 * A solid surface with a hairline edge: Compose has no backdrop blur that works on every API level
 * this app supports (minSdk 26), so the bar stays opaque rather than faking glass.
 */
@Composable
private fun AppTabBar(currentRoute: String, pendingApprovals: Int, onSelect: (Screen) -> Unit) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth().background(c.card)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.hairline))
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp).height(60.dp)) {
            bottomBarScreens.forEach { screen ->
                val selected = isTabSelected(screen, currentRoute)
                val color = if (selected) c.accent else c.textMuted
                Column(
                    Modifier
                        .weight(1f)
                        .height(60.dp)
                        .semantics { this.selected = selected }
                        .tappable(role = Role.Tab, pressedScale = 0.94f, onClick = { onSelect(screen) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Box {
                        Icon(screen.icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                        if (screen == Screen.Approvals && pendingApprovals > 0) {
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 9.dp, y = (-5).dp)
                                    .widthIn(min = 16.dp)
                                    .height(16.dp)
                                    .clip(CircleShape)
                                    .background(c.accent)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (pendingApprovals > 99) "99+" else pendingApprovals.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.onAccent
                                )
                            }
                        }
                    }
                    Text(
                        screen.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }
    }
}
