package app.oubliettes

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** One colour scheme of the app: [bg] behind everything, [gold] for accents and titles, [glow] for the neon halos. */
internal class MathraTheme(
    val name: String, val emoji: String,
    val bg: Color, val ink: Color, val dim: Color, val gold: Color, val glow: Color, val glow2: Color,
)

internal val THEMES = listOf(
    MathraTheme("Teddy Gold", "🧸", Color(0xFF1B1A22), Color(0xFFF2F0FA), Color(0xFF8D8AA3), Color(0xFFE0B84C), Color(0xFFFFB300), Color(0xFF2979FF)),
    MathraTheme("Neon Blue", "💙", Color(0xFF0B1020), Color(0xFFEAF2FF), Color(0xFF7F93B8), Color(0xFF4FC3F7), Color(0xFF00B0FF), Color(0xFF7C4DFF)),
    MathraTheme("Emerald", "💚", Color(0xFF08160F), Color(0xFFE8FFF1), Color(0xFF7FA891), Color(0xFF3DDC84), Color(0xFF00E676), Color(0xFF1DE9B6)),
    MathraTheme("Crimson", "❤️", Color(0xFF1C0B10), Color(0xFFFFEDEF), Color(0xFFB08A90), Color(0xFFFF5252), Color(0xFFFF1744), Color(0xFFFF9100)),
    MathraTheme("Royal Violet", "💜", Color(0xFF140B22), Color(0xFFF4EBFF), Color(0xFF9A86B8), Color(0xFFB388FF), Color(0xFFD500F9), Color(0xFF536DFE)),
    MathraTheme("Sunset", "🌅", Color(0xFF1E0F12), Color(0xFFFFF1E6), Color(0xFFB39485), Color(0xFFFF8A3D), Color(0xFFFF6D00), Color(0xFFFF4081)),
    MathraTheme("Midnight", "🌙", Color(0xFF05060A), Color(0xFFE6E8F0), Color(0xFF70758A), Color(0xFFCFD8DC), Color(0xFF90A4AE), Color(0xFF448AFF)),
    MathraTheme("Candy", "🍬", Color(0xFF1F0E1E), Color(0xFFFFEAF8), Color(0xFFB88AAE), Color(0xFFFF80AB), Color(0xFFF50057), Color(0xFF18FFFF)),
)

/** The theme in use. Colours are read through [Bg], [Ink], [Dim] and [Gold], so changing [index] repaints everything at once. */
internal object AppTheme {
    var index by mutableIntStateOf(0)
    val current get() = THEMES[index.coerceIn(THEMES.indices)]
    val bg get() = current.bg
    val ink get() = current.ink
    val dim get() = current.dim
    val gold get() = current.gold
    val glow get() = current.glow
    val glow2 get() = current.glow2

    fun load(prefs: SharedPreferences) {
        index = prefs.getInt("theme", 0).coerceIn(THEMES.indices)
    }

    fun choose(prefs: SharedPreferences, value: Int) {
        index = value.coerceIn(THEMES.indices)
        prefs.edit().putInt("theme", index).apply()
    }
}
