package com.personalai.os.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.personalai.os.ui.Screen
import com.personalai.os.ui.components.GroupedCard
import com.personalai.os.ui.components.ListRow
import com.personalai.os.ui.components.RowDivider
import com.personalai.os.ui.components.ScreenHeader
import com.personalai.os.ui.components.fadeEdges
import com.personalai.os.ui.moreScreens
import com.personalai.os.ui.theme.AppShapes
import com.personalai.os.ui.theme.AppTheme

@Composable
fun MoreScreen(onOpen: (Screen) -> Unit) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "More")
        Column(Modifier.weight(1f).fadeEdges().verticalScroll(rememberScrollState())) {
            GroupedCard(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                moreScreens.forEachIndexed { i, screen ->
                    if (i > 0) RowDivider(startInset = 70.dp)
                    ListRow(
                        title = screen.title,
                        subtitle = screen.summary,
                        onClick = { onOpen(screen) },
                        leading = {
                            Box(
                                Modifier.size(40.dp).clip(AppShapes.control).background(c.raised),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(screen.icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = c.text)
                            }
                        },
                        trailing = {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = c.textMuted)
                        }
                    )
                }
            }
        }
    }
}
