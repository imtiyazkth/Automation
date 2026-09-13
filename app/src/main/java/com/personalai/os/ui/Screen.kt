package com.personalai.os.ui

sealed class Screen(val route: String, val label: String, val emoji: String) {
    object Chat : Screen("chat", "Chat", "\uD83D\uDCAC")
    object Dashboard : Screen("dashboard", "Home", "\uD83C\uDFE0")
    object Approvals : Screen("approvals", "Approvals", "\u2705")
    object Agents : Screen("agents", "Agents", "\uD83E\uDD16")
    object Permissions : Screen("permissions", "Permissions", "\uD83D\uDD10")
    object Audit : Screen("audit", "Audit", "\uD83D\uDCDC")

    companion object {
        val bottomBarScreens = listOf(Chat, Dashboard, Approvals, Agents, Permissions, Audit)
    }
}
