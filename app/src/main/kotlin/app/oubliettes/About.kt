package app.oubliettes

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.launch

/** A blackletter title with a solid extruded edge: the same text stacked a little lower and further right, darker each time. */
@Composable
private fun ExtrudedTitle(text: String, size: Int) {
    val edge = AppTheme.glow2
    Box {
        for (k in 9 downTo 1) {
            Text(
                text, Modifier.offset((k * 0.7f).dp, (k * 1.0f).dp),
                color = lerp(edge, Color.Black, 0.25f + k * 0.07f),
                style = TextStyle(fontFamily = Fraktur, fontSize = size.sp),
            )
        }
        Text(
            text, color = Gold,
            style = TextStyle(fontFamily = Fraktur, fontSize = size.sp, shadow = Shadow(AppTheme.glow, Offset(0f, 0f), 24f)),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun About(onMenu: () -> Unit) {
    val context = LocalContext.current
    val version = remember { runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty() }
    val tiltX = remember { Animatable(0f) }
    val tiltY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val idle = rememberInfiniteTransition(label = "idle").animateFloat(0f, 2f * PI.toFloat(), infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "sway")
    val settle = {
        scope.launch { tiltX.animateTo(0f, spring(0.5f, 60f)) }
        scope.launch { tiltY.animateTo(0f, spring(0.5f, 60f)) }
        Unit
    }
    Page("About", onMenu) {
        // The bear turns under a finger, sways on its own and springs back.
        Box(
            Modifier.size(280.dp).pointerInput(Unit) {
                detectDragGestures(onDragEnd = { settle() }, onDragCancel = { settle() }) { change, drag ->
                    change.consume()
                    scope.launch {
                        tiltY.snapTo(tiltY.value + drag.x * 0.4f)
                        tiltX.snapTo((tiltX.value - drag.y * 0.4f).coerceIn(-35f, 35f))
                    }
                }
            },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.fillMaxSize().graphicsLayer {
                    cameraDistance = 14f * density
                    rotationY = tiltY.value + if (Comfort.calm) 0f else sin(idle.value) * 14f
                    rotationX = tiltX.value
                    translationY = if (Comfort.calm) 0f else sin(idle.value * 2f) * 6f * density
                },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val r = size.width * 0.5f
                    drawCircle(Brush.radialGradient(listOf(AppTheme.glow.copy(alpha = 0.5f), AppTheme.glow2.copy(alpha = 0.2f), Color.Transparent), center, r), r)
                }
                Image(
                    painterResource(R.drawable.mathra_logo), "Mathra Oubliettes logo",
                    Modifier.size(230.dp).clip(CircleShape).border(3.dp, Brush.linearGradient(listOf(AppTheme.glow, AppTheme.glow2)), CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Canvas(Modifier.size(200.dp, 16.dp)) {
            drawOval(Brush.radialGradient(listOf(AppTheme.glow.copy(alpha = 0.45f), Color.Transparent), center, size.width / 2f), size = size)
        }
        ExtrudedTitle("Mathra", 34)
        ExtrudedTitle("Oubliettes", 48)
        Text(if (version.isEmpty()) "Dungeon logic puzzle" else "Dungeon logic puzzle · v$version", color = Dim, fontSize = 15.sp)

        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((emoji, text) in listOf(
                "🧩" to "${LEVELS * SIZES.size} levels", "♾️" to "Endless", "📅" to "Daily",
                "🎨" to "${THEMES.size} themes", "⚡" to "${Fx.entries.size} effects", "💾" to "Backup",
            )) NeonChip(text, emoji, selected = true) {}
        }

        NeonCard(Modifier.fillMaxWidth()) {
            Text("About the game", color = Gold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(
                "Work out where the dungeon walls are from the clues on the edge of the grid. Every grid has exactly one " +
                    "solution: find the monsters' dead ends, the treasure rooms and the hidden hallways, one mark at a time.",
                color = Ink, fontSize = 16.sp, textAlign = TextAlign.Center,
            )
        }

        NeonCard(Modifier.fillMaxWidth()) {
            Text("Developer", color = Dim, fontSize = 14.sp)
            Text("👨‍💻", fontSize = 36.sp)
            Text("Denuwan Akash", color = Ink, fontWeight = FontWeight.Bold, fontSize = 26.sp, textAlign = TextAlign.Center)
            Text("දෙනුවන් ආකාශ්", color = Gold, fontSize = 22.sp, textAlign = TextAlign.Center)
        }

        Row2(
            { NeonButton("Spin", Modifier.weight(1f), emoji = "🔄") {
                scope.launch {
                    tiltY.snapTo(0f)
                    tiltY.animateTo(360f, tween(1100))
                    tiltY.snapTo(0f)
                }
            } },
            { NeonButton("Share", Modifier.weight(1f), emoji = "📤") {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Mathra Oubliettes: a dungeon logic puzzle by Denuwan Akash 🧸⚡")
                }
                context.startActivity(Intent.createChooser(send, null))
            } },
        )

        NeonCard(Modifier.fillMaxWidth()) {
            Text("Follow and contact", color = Gold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("@mathra_quotes_1", color = Ink, fontSize = 17.sp)
            Text("📞 0783756055", color = Ink, fontSize = 17.sp)
        }
        Row2(
            { NeonButton("TikTok", Modifier.weight(1f), emoji = "🎵") {
                context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.tiktok.com/@mathra_quotes_1?_r=1&_t=ZS-9AeWvROfUEJ")))
            } },
            { NeonButton("Call", Modifier.weight(1f), emoji = "📞") {
                context.startActivity(Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:0783756055")))
            } },
        )

        Text(
            "Based on the open-source game Oubliettes by all3f0r1, under the GNU GPL v3. " +
                "Fonts: Almendra and UnifrakturCook (SIL OFL).",
            color = Dim, fontSize = 12.sp, textAlign = TextAlign.Center,
        )
    }
}

/** Two buttons side by side, each given its share of the row. */
@Composable
private fun Row2(left: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit, right: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        left()
        right()
    }
}
