package com.personalai.os.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.personalai.os.ui.theme.AppIcons

/**
 * [label] is the tab name; [title] is the screen heading. Five tabs fit a thumb;
 * the rest live one level down under More.
 */
sealed class Screen(
    val route: String,
    val label: String,
    val title: String,
    val icon: ImageVector,
    val summary: String = ""
) {
    object Chat : Screen("chat", "Chat", "Head agent", AppIcons.Chat)
    object Dashboard : Screen("dashboard", "Today", "Today", Icons.Default.Home)
    object Approvals : Screen("approvals", "Approvals", "Approvals", Icons.Default.CheckCircle)
    object Agents : Screen("agents", "Agents", "Agents", Icons.Default.Person)
    object More : Screen("more", "More", "More", Icons.Default.Menu)

    object Scheduled : Screen("scheduled", "Tasks", "Tasks", Icons.Default.DateRange, "Automations that run on a schedule")
    object Permissions : Screen("permissions", "Permissions", "Permissions", Icons.Default.Lock, "What each agent may access")
    object Audit : Screen("audit", "Activity", "Activity", Icons.AutoMirrored.Filled.List, "A record of what your agents did")
    object Diagnostics : Screen("diagnostics", "Diagnostics", "Diagnostics", Icons.Default.Build, "Crash and error logs")
}

val bottomBarScreens = listOf(Screen.Chat, Screen.Dashboard, Screen.Approvals, Screen.Agents, Screen.More)

/** Screens opened from More. They push onto the stack with a back button. */
val moreScreens = listOf(Screen.Scheduled, Screen.Permissions, Screen.Audit, Screen.Diagnostics)
