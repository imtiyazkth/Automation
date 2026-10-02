package com.personalai.os.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.personalai.os.ui.theme.AppShapes
import com.personalai.os.ui.theme.AppTheme

/**
 * One approval card, shared by the Chat thread and the Approvals screen so they
 * look and behave identically. Send is the primary action; Ignore is quiet.
 */
@Composable
fun ApprovalCardContent(
    summary: String,
    reason: String,
    message: String?,
    canEdit: Boolean,
    onSend: () -> Unit,
    onSendEdited: (String) -> Unit,
    onIgnore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = AppTheme.colors
    val reduced = rememberReducedMotion()
    var editing by remember { mutableStateOf(false) }
    var editedText by remember(message) { mutableStateOf(message ?: "") }
    val sizeSpec: FiniteAnimationSpec<IntSize> =
        if (reduced) snap() else spring(dampingRatio = 1f, stiffness = 400f)

    AppCard(modifier.animateContentSize(animationSpec = sizeSpec)) {
        Column(Modifier.padding(16.dp)) {
            StatusPill("Needs your approval", tone = Tone.Warning)
            Text(summary, style = MaterialTheme.typography.titleMedium, color = c.text, modifier = Modifier.padding(top = 12.dp))
            Text(reason, style = MaterialTheme.typography.bodyMedium, color = c.textMuted, modifier = Modifier.padding(top = 4.dp))

            if (editing) {
                AppTextField(
                    value = editedText, onValueChange = { editedText = it },
                    label = "Message", modifier = Modifier.padding(top = 12.dp)
                )
            } else if (!message.isNullOrBlank()) {
                Box(
                    Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(AppShapes.inset)
                        .background(c.raised)
                        .padding(12.dp)
                ) {
                    Text(editedText, style = MaterialTheme.typography.bodyMedium, color = c.text)
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (editing) {
                    AppButton("Send edited", onClick = { onSendEdited(editedText); editing = false }, compact = true)
                    AppButton("Cancel", onClick = { editedText = message ?: ""; editing = false }, kind = ButtonKind.Quiet, compact = true)
                } else {
                    AppButton("Send", onClick = onSend, compact = true)
                    if (canEdit) AppButton("Edit", onClick = { editing = true }, kind = ButtonKind.Secondary, compact = true)
                    Spacer(Modifier.weight(1f))
                    AppButton("Ignore", onClick = onIgnore, kind = ButtonKind.Quiet, compact = true)
                }
            }
        }
    }
}
