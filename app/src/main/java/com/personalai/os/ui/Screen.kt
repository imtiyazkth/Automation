package com.personalai.os.ui

sealed class Screen(val route: String, val label: String, val emoji: String) {
    object Chat : Screen("chat", "Chat", "\uD83D\uDCAC")
    object Dashboard : Screen("dashboard", "Home", "\uD83C\uDFE0")
    object Approvals : Screen("approvals", "Approve", "\u2705")
    object Agents : Screen("agents", "Agents", "\uD83E\uDD16")
    object Permissions : Screen("permissions", "Perms", "\uD83D\uDD10")
    object Audit : Screen("audit", "Audit", "\uD83D\uDCDC")
    object Diagnostics : Screen("diagnostics", "Diag", "\uD83E\uDE7A")
}

val bottomBarScreens = listOf(
    Screen.Chat, Screen.Dashboard, Screen.Approvals, Screen.Agents,
    Screen.Permissions, Screen.Audit, Screen.Diagnostics
)
