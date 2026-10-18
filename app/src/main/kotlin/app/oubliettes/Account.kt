package app.oubliettes

import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Avatars to pick from; the first is the bear of the logo. */
internal val AVATARS = listOf("🧸", "😎", "👑", "🦁", "🐺", "🦊", "🐉", "🦄", "🤖", "👻", "🔥", "⚡")

private val RANKS = listOf("Rookie", "Digger", "Delver", "Trapfinder", "Dungeon Master", "Vault Keeper", "Legend", "Oubliette King")

/** What the player has done so far, as the dashboard and the account show it. */
internal class PlayerStats(val solved: Int, val daily: Int, val finished: Int, val tutorialDone: Boolean) {
    val xp get() = solved * 20 + daily * 30 + finished * 100
    val level get() = xp / 100 + 1
    val rank get() = RANKS[(level - 1).coerceAtMost(RANKS.lastIndex)]
    val progress get() = (xp % 100) / 100f
}

internal fun playerStats(prefs: SharedPreferences) = PlayerStats(
    solved = SIZES.sumOf { prefs.solved(it) },
    daily = prefs.all.keys.count { it.startsWith("dailyDone") },
    finished = SIZES.count { prefs.solved(it) >= LEVELS },
    tutorialDone = prefs.getBoolean("tutorialDone", false),
)

/** The player's picture: the bear, or an emoji, in a neon ring. */
@Composable
internal fun Avatar(index: Int, size: Dp) {
    val ring = Brush.linearGradient(listOf(AppTheme.glow, AppTheme.glow2))
    Box(Modifier.size(size).clip(CircleShape).border(2.5.dp, ring, CircleShape).background(Bg), contentAlignment = Alignment.Center) {
        if (index == 0) Image(painterResource(R.drawable.mathra_logo), null, Modifier.fillMaxHeight().fillMaxWidth(), contentScale = ContentScale.Crop)
        else Text(AVATARS[index.coerceIn(AVATARS.indices)], fontSize = (size.value * 0.52f).sp)
    }
}

/** A thin bar filled with the neon gradient up to [fraction]. */
@Composable
internal fun NeonBar(fraction: Float, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(10.dp)) {
        val corner = CornerRadius(size.height / 2f)
        drawRoundRect(Color.White.copy(alpha = 0.1f), cornerRadius = corner)
        if (fraction > 0f) drawRoundRect(
            Brush.horizontalGradient(listOf(AppTheme.glow, AppTheme.glow2)),
            size = Size(size.width * fraction.coerceIn(0.03f, 1f), size.height), cornerRadius = corner,
        )
    }
}

private class Badge(val emoji: String, val name: String, val earned: (PlayerStats) -> Boolean)

private val BADGES = listOf(
    Badge("🎓", "Tutorial") { it.tutorialDone },
    Badge("🥇", "First dungeon") { it.solved >= 1 },
    Badge("🔥", "10 solved") { it.solved >= 10 },
    Badge("🏅", "50 solved") { it.solved >= 50 },
    Badge("👑", "100 solved") { it.solved >= 100 },
    Badge("📅", "Daily done") { it.daily >= 1 },
    Badge("🗓️", "7 dailies") { it.daily >= 7 },
    Badge("🏰", "Chapter done") { it.finished >= 1 },
    Badge("💎", "All chapters") { it.finished >= SIZES.size },
)

/**
 * The player's card, kept on this phone: a name, a picture, a level that grows with the dungeons solved,
 * and the badges earned. There is no sign-in: the progress file in Options is how it moves to another phone.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Account(prefs: SharedPreferences, onMenu: () -> Unit, open: (Screen) -> Unit) {
    var name by remember { mutableStateOf(prefs.getString("playerName", "Player")!!) }
    var avatar by remember { mutableIntStateOf(prefs.getInt("avatar", 0).coerceIn(AVATARS.indices)) }
    var editing by rememberSaveable { mutableStateOf(false) }
    val stats = playerStats(prefs)
    Page("Account", onMenu) {
        NeonCard(Modifier.fillMaxWidth()) {
            Avatar(avatar, 104.dp)
            Text(name, color = Ink, fontWeight = FontWeight.Bold, fontSize = 28.sp, textAlign = TextAlign.Center)
            Text("🏅 ${stats.rank} · Level ${stats.level}", color = Gold, fontSize = 18.sp)
            NeonBar(stats.progress)
            Text("${stats.xp % 100} / 100 XP to level ${stats.level + 1}", color = Dim, fontSize = 13.sp)
        }

        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCell("🏆", "${stats.solved}/${LEVELS * SIZES.size}", "Solved", Modifier.weight(1f).fillMaxHeight())
            StatCell("📅", "${stats.daily}", "Dailies", Modifier.weight(1f).fillMaxHeight())
            StatCell("🏰", "${stats.finished}/${SIZES.size}", "Chapters", Modifier.weight(1f).fillMaxHeight())
        }

        SectionTitle("🖼️", "Avatar")
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((index, emoji) in AVATARS.withIndex()) {
                val shape = RoundedCornerShape(14.dp)
                Box(
                    Modifier.size(48.dp).clip(shape)
                        .background(if (index == avatar) AppTheme.glow.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.06f))
                        .border(2.dp, if (index == avatar) AppTheme.glow else Dim.copy(alpha = 0.4f), shape)
                        .clickable { avatar = index; prefs.edit().putInt("avatar", index).apply() },
                    contentAlignment = Alignment.Center,
                ) { Text(emoji, fontSize = 24.sp) }
            }
        }

        SectionTitle("🎖️", "Badges")
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (badge in BADGES) {
                val got = badge.earned(stats)
                NeonChip(badge.name, if (got) badge.emoji else "🔒", got, Modifier.alpha(if (got) 1f else 0.55f)) {}
            }
        }

        NeonButton("Edit name", Modifier.fillMaxWidth(), emoji = "✏️") { editing = true }
        NeonButton("Backup & settings", Modifier.fillMaxWidth(), emoji = "⚙️") { open(Screen.OPTIONS) }
    }
    if (editing) Notice("Your name", { editing = false }) {
        var draft by remember { mutableStateOf(name) }
        OutlinedTextField(draft, { draft = it.take(16) }, singleLine = true)
        Planks {
            PlankButton("Cancel") { editing = false }
            PlankButton("Save") {
                name = draft.trim().ifEmpty { "Player" }
                prefs.edit().putString("playerName", name).apply()
                editing = false
            }
        }
    }
}
