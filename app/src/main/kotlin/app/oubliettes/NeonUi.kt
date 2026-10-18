package app.oubliettes

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A glowing glass button: a dark pane with a neon gradient rim that sinks and tilts a little under the finger.
 * [accent] and [accent2] give the rim; they follow the theme unless told otherwise.
 */
@Composable
internal fun NeonButton(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    accent: Color = AppTheme.glow,
    accent2: Color = AppTheme.glow2,
    onClick: () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 1f else 0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "press")
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .graphicsLayer {
                cameraDistance = 14f * density
                rotationX = 14f * press
                scaleX = 1f - 0.04f * press
                scaleY = 1f - 0.04f * press
            }
            .shadow(if (pressed) 2.dp else 10.dp, shape, ambientColor = accent, spotColor = accent)
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.30f), Bg.copy(alpha = 0.92f), accent2.copy(alpha = 0.30f))))
            .border(BorderStroke(2.dp, Brush.horizontalGradient(listOf(accent, accent2))), shape)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 52.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (emoji != null) Text(emoji, fontSize = 22.sp)
        Text(text, color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
    }
}

/** A small round-cornered choice: lit with the accent colour when [selected]. */
@Composable
internal fun NeonChip(text: String, emoji: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val accent = AppTheme.glow
    Row(
        modifier
            .semantics { this.selected = selected }
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.05f))
            .border(1.5.dp, if (selected) accent else Dim.copy(alpha = 0.5f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 40.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 17.sp)
        Text(text, color = if (selected) Ink else Dim, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

/** A glass card with a thin neon rim, for stats and sections. */
@Composable
internal fun NeonCard(modifier: Modifier = Modifier, padding: androidx.compose.ui.unit.Dp = 14.dp, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.02f))))
            .border(BorderStroke(1.5.dp, Brush.linearGradient(listOf(AppTheme.glow.copy(alpha = 0.8f), AppTheme.glow2.copy(alpha = 0.8f)))), shape)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

/** A figure with its emoji and a caption: one cell of a stats strip. */
@Composable
internal fun StatCell(emoji: String, value: String, caption: String, modifier: Modifier = Modifier) {
    NeonCard(modifier, padding = 8.dp) {
        Text(emoji, fontSize = 18.sp)
        Text(value, color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
        Text(caption, color = Dim, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

/** A section title with its emoji. */
@Composable
internal fun SectionTitle(emoji: String, text: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 20.sp)
        Text(text, color = Gold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}
