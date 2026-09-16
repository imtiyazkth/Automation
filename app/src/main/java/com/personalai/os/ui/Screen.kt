package com.personalai.os.ui

sealed class Screen(val route: String, val label: String, val emoji: String) {
    object Chat : Screen("chat", "Chat", "\uD83D\uDCAC")
    object Dashboard : Screen("dashboard", "Home", "\uD83C\uDFE0")
    object Approvals : Screen("approvals", "Approvals", "\u2705")
    object Agents : Screen("agents", "Agents", "\uD83E\uDD16")
    object Permissions : Screen("permissions", "Permissions", "\uD83D\uDD10")
    object Audit : Screen("audit", "Audit", "\uD83D\uDCDC")
    object Diagnostics : Screen("diagnostics", "Diagnostics", "\uD83E\uDE7A")
}

// Deliberately a top-level property, NOT inside Screen's own companion
// object - a companion referencing sibling nested `object`s of the same
// sealed class hits a class-initialization-order bug on some Android
// versions (confirmed: Android 10 / API 29), surfacing as a
// NullPointerException the very first time this list is read. A top-level
// val in the same file has no such circular-initialization risk.
val bottomBarScreens = listOf(
    Screen.Chat, Screen.Dashboard, Screen.Approvals, Screen.Agents,
    Screen.Permissions, Screen.Audit, Screen.Diagnostics
)
