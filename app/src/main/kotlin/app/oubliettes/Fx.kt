package app.oubliettes

import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** The dashboard effects, fifteen of them. [default] ones are on at first. */
internal enum class Fx(val label: String, val emoji: String, val default: Boolean = false) {
    LIGHTNING("Lightning", "⚡", true),
    RAIN("Rain", "🌧️", true),
    SNOW("Snow", "❄️", true),
    STARS("Stars", "✨"),
    METEORS("Meteors", "☄️"),
    FIREFLIES("Fireflies", "💫"),
    EMBERS("Embers", "🔥"),
    FOG("Fog", "🌫️"),
    AURORA("Aurora", "🌌"),
    BUBBLES("Bubbles", "🫧"),
    PETALS("Petals", "🌸"),
    MATRIX("Digital rain", "💻"),
    CONFETTI("Confetti", "🎉"),
    FIREWORKS("Fireworks", "🎆"),
    PULSE("Neon rings", "💠"),
}

/** Which effects are on, kept in the preferences under fx_master and fx_<NAME>. */
internal object FxSettings {
    var master by mutableStateOf(true)
    private val on = mutableStateMapOf<Fx, Boolean>()

    fun load(prefs: SharedPreferences) {
        master = prefs.getBoolean("fx_master", true)
        for (fx in Fx.entries) on[fx] = prefs.getBoolean("fx_${fx.name}", fx.default)
    }

    fun isOn(fx: Fx) = on[fx] == true

    fun toggle(prefs: SharedPreferences, fx: Fx) {
        val value = !isOn(fx)
        on[fx] = value
        prefs.edit().putBoolean("fx_${fx.name}", value).apply()
    }

    fun setMaster(prefs: SharedPreferences, value: Boolean) {
        master = value
        prefs.edit().putBoolean("fx_master", value).apply()
    }

    fun setAll(prefs: SharedPreferences, value: Boolean) {
        val editor = prefs.edit()
        for (fx in Fx.entries) {
            on[fx] = value
            editor.putBoolean("fx_${fx.name}", value)
        }
        editor.apply()
    }
}

/** Seconds since the layer appeared, updated on every frame. Read it while drawing. */
@Composable
private fun rememberFrameTime(): State<Float> = produceState(0f) {
    var first = -1L
    while (true) {
        withFrameNanos { now ->
            if (first < 0) first = now
            value = (now - first) / 1_000_000_000f
        }
    }
}

/** A repeatable pseudo-random number in 0..1 for the [i]th element of the effect [salt]. */
private fun h(i: Int, salt: Int): Float {
    var x = i * 374761393 + salt * 668265263
    x = (x xor (x ushr 13)) * 1274126177
    x = x xor (x ushr 16)
    return (x and 0xFFFF) / 65535f
}

/**
 * Every effect that is switched on, drawn over the whole screen. It never takes a touch. Nothing in calm
 * mode or when the master switch is off. Lightning goes last so its flash lights the rest.
 */
@Composable
internal fun FxLayer(modifier: Modifier = Modifier) {
    if (Comfort.calm || !FxSettings.master) return
    val active = Fx.entries.filter { FxSettings.isOn(it) }.sortedBy { it == Fx.LIGHTNING }
    if (active.isEmpty()) return
    val time = rememberFrameTime()
    Canvas(modifier.fillMaxSize()) {
        val t = time.value
        for (fx in active) when (fx) {
            Fx.LIGHTNING -> lightning(t)
            Fx.RAIN -> rain(t)
            Fx.SNOW -> snow(t)
            Fx.STARS -> stars(t)
            Fx.METEORS -> meteors(t)
            Fx.FIREFLIES -> fireflies(t)
            Fx.EMBERS -> embers(t)
            Fx.FOG -> fog(t)
            Fx.AURORA -> aurora(t)
            Fx.BUBBLES -> bubbles(t)
            Fx.PETALS -> petals(t)
            Fx.MATRIX -> matrix(t)
            Fx.CONFETTI -> confetti(t)
            Fx.FIREWORKS -> fireworks(t)
            Fx.PULSE -> pulse(t)
        }
    }
}

/** The row of switches under the logo: all effects on or off, then one chip each. */
@Composable
internal fun FxBar(prefs: SharedPreferences, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NeonChip(if (FxSettings.master) "FX on" else "FX off", "🎛️", FxSettings.master) { FxSettings.setMaster(prefs, !FxSettings.master) }
        for (fx in Fx.entries) NeonChip(fx.label, fx.emoji, FxSettings.master && FxSettings.isOn(fx)) {
            if (!FxSettings.master) FxSettings.setMaster(prefs, true)
            FxSettings.toggle(prefs, fx)
        }
    }
}

private fun DrawScope.rain(t: Float) {
    val dp = density
    val color = AppTheme.glow2
    for (i in 0 until 90) {
        val speed = (300f + 420f * h(i, 2)) * dp
        val length = (14f + 20f * h(i, 3)) * dp
        val travel = size.height + length
        val y = (t * speed + h(i, 4) * travel) % travel - length
        val x = h(i, 1) * size.width
        drawLine(color.copy(alpha = 0.2f + 0.3f * h(i, 5)), Offset(x, y), Offset(x - length * 0.2f, y + length), 1.3f * dp, StrokeCap.Round)
    }
}

private fun DrawScope.snow(t: Float) {
    val dp = density
    for (i in 0 until 70) {
        val radius = (1.2f + 2.8f * h(i, 2)) * dp
        val speed = (22f + 50f * h(i, 3)) * dp
        val span = size.height + 20 * dp
        val y = (t * speed + h(i, 4) * span) % span - 10 * dp
        val x = h(i, 1) * size.width + sin(t * (0.6f + h(i, 5)) + i) * 18 * dp
        drawCircle(Color.White.copy(alpha = 0.5f + 0.4f * h(i, 6)), radius, Offset(x, y))
    }
}

private fun DrawScope.stars(t: Float) {
    val dp = density
    for (i in 0 until 60) {
        val at = Offset(h(i, 1) * size.width, h(i, 2) * size.height * 0.85f)
        val twinkle = 0.5f + 0.5f * sin(t * (1.2f + 2f * h(i, 3)) + i * 1.7f)
        val radius = (0.8f + 1.6f * h(i, 4)) * dp
        val color = Color.White.copy(alpha = 0.2f + 0.7f * twinkle)
        drawCircle(color, radius, at)
        if (i % 6 == 0) {
            val arm = radius * (3f + 3f * twinkle)
            drawLine(color, at - Offset(arm, 0f), at + Offset(arm, 0f), 1.dp.toPx())
            drawLine(color, at - Offset(0f, arm), at + Offset(0f, arm), 1.dp.toPx())
        }
    }
}

private fun DrawScope.meteors(t: Float) {
    val dp = density
    for (i in 0 until 4) {
        val period = 4f + 2.5f * i
        val local = t + i * 1.9f
        val phase = (local % period) / 1.1f
        if (phase > 1f) continue
        val n = (local / period).toInt()
        val start = Offset((0.3f + 0.7f * h(n * 7 + i, 1)) * size.width, h(n * 5 + i, 2) * size.height * 0.4f)
        val dir = Offset(-0.8f, 0.6f)
        val head = start + dir * (phase * size.width * 0.7f)
        val tail = head - dir * (70 * dp)
        val fade = 1f - phase
        drawLine(
            Brush.linearGradient(listOf(Color.Transparent, Color.White.copy(alpha = fade)), tail, head),
            tail, head, 2.5f * dp, StrokeCap.Round,
        )
        drawCircle(Color.White.copy(alpha = fade), 2.5f * dp, head)
    }
}

private fun DrawScope.fireflies(t: Float) {
    val dp = density
    for (i in 0 until 24) {
        val centre = Offset(
            h(i, 1) * size.width + sin(t * 0.4f * (0.5f + h(i, 2)) + i) * 40 * dp,
            h(i, 3) * size.height + cos(t * 0.3f * (0.5f + h(i, 4)) + i) * 40 * dp,
        )
        val pulse = 0.5f + 0.5f * sin(t * (1.5f + h(i, 5)) + i)
        val radius = 16 * dp * (0.6f + 0.4f * pulse)
        val glow = Color(0xFFEFFF6B)
        drawCircle(Brush.radialGradient(listOf(glow.copy(alpha = 0.85f * pulse), Color.Transparent), centre, radius), radius, centre)
        drawCircle(glow.copy(alpha = 0.4f + 0.6f * pulse), 2 * dp, centre)
    }
}

private fun DrawScope.embers(t: Float) {
    val dp = density
    for (i in 0 until 40) {
        val life = (t * (0.08f + 0.12f * h(i, 2)) + h(i, 3)) % 1f
        val at = Offset(h(i, 1) * size.width + sin(life * 8f + i) * 20 * dp, size.height - life * size.height * 0.9f)
        val color = lerp(Color(0xFFFFD54F), Color(0xFFFF5722), life)
        drawCircle(color.copy(alpha = 1f - life), (1.2f + 2.2f * h(i, 4)) * dp, at)
    }
}

private fun DrawScope.fog(t: Float) {
    val dp = density
    for (i in 0 until 5) {
        val span = size.width * 1.6f
        val x = (t * (8f + 6f * i) * dp * 0.3f + i * size.width / 3f) % span - size.width * 0.3f
        val centre = Offset(x, size.height * (0.2f + 0.17f * i))
        val radius = size.width * 0.55f
        drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.10f), Color.Transparent), centre, radius), radius, centre)
    }
}

private fun DrawScope.aurora(t: Float) {
    val steps = 24
    for (band in 0..2) {
        val colour = if (band % 2 == 0) AppTheme.glow else AppTheme.glow2
        val top = ArrayList<Offset>()
        for (k in 0..steps) {
            val x = size.width * k / steps
            top.add(Offset(x, size.height * (0.12f + 0.05f * band) + sin(x / size.width * 6f + t * 0.5f + band) * size.height * 0.04f))
        }
        val path = Path().apply {
            moveTo(top[0].x, top[0].y)
            for (p in top) lineTo(p.x, p.y)
            for (p in top.asReversed()) lineTo(p.x, p.y + size.height * 0.18f)
            close()
        }
        drawPath(path, Brush.verticalGradient(listOf(Color.Transparent, colour.copy(alpha = 0.28f), Color.Transparent), startY = size.height * 0.05f, endY = size.height * 0.42f))
    }
}

private fun DrawScope.bubbles(t: Float) {
    val dp = density
    for (i in 0 until 22) {
        val life = (t * (0.05f + 0.08f * h(i, 2)) + h(i, 3)) % 1f
        val r = (5f + 12f * h(i, 4)) * dp
        val c = Offset(h(i, 1) * size.width + sin(life * 10f + i) * 14 * dp, size.height + r - life * (size.height + 2 * r))
        drawCircle(Color.White.copy(alpha = 0.35f), r, c, style = Stroke(1.2f * dp))
        drawCircle(Color.White.copy(alpha = 0.5f), r * 0.18f, c + Offset(-r * 0.4f, -r * 0.4f))
    }
}

private fun DrawScope.petals(t: Float) {
    val dp = density
    for (i in 0 until 28) {
        val life = (t * (0.05f + 0.06f * h(i, 2)) + h(i, 3)) % 1f
        val x = h(i, 1) * size.width + sin(life * 9f + i) * 30 * dp
        val y = -20 * dp + life * (size.height + 40 * dp)
        rotate(t * 60f * (0.5f + h(i, 4)) + i * 40f, Offset(x, y)) {
            drawOval(Color(0xFFFF9EC4).copy(alpha = 0.8f), Offset(x - 5 * dp, y - 3 * dp), Size(10 * dp, 6 * dp))
        }
    }
}

private fun DrawScope.matrix(t: Float) {
    val dp = density
    val column = 16 * dp
    val cell = 14 * dp
    val green = Color(0xFF00E676)
    for (c in 0 until (size.width / column).toInt()) {
        val speed = (110f + 250f * h(c, 1)) * dp
        val head = (t * speed + h(c, 2) * size.height * 2f) % (size.height * 1.6f)
        for (k in 0 until 10) {
            val y = head - k * cell
            if (y < 0f || y > size.height) continue
            if (h(c * 31 + k + (t * 6f).toInt(), 3) < 0.3f) continue
            val colour = if (k == 0) Color.White else green.copy(alpha = 0.7f * (1f - k / 10f))
            drawRect(colour, Offset(c * column + 3 * dp, y), Size(column - 8 * dp, cell - 5 * dp))
        }
    }
}

private val CONFETTI = listOf(Color(0xFFFF5252), Color(0xFFFFD740), Color(0xFF69F0AE), Color(0xFF40C4FF), Color(0xFFE040FB))

private fun DrawScope.confetti(t: Float) {
    val dp = density
    for (i in 0 until 40) {
        val life = (t * (0.1f + 0.1f * h(i, 2)) + h(i, 3)) % 1f
        val x = h(i, 1) * size.width + sin(life * 7f + i) * 25 * dp
        val y = -10 * dp + life * (size.height + 20 * dp)
        rotate(t * 180f * (0.5f + h(i, 4)) + i * 30f, Offset(x, y)) {
            drawRect(CONFETTI[i % CONFETTI.size], Offset(x - 3.5f * dp, y - 2 * dp), Size(7 * dp, 4 * dp))
        }
    }
}

private fun DrawScope.fireworks(t: Float) {
    val dp = density
    val period = 4.5f
    for (b in 0 until 3) {
        val local = t + b * 1.5f
        val n = (local / period).toInt()
        val p = (local % period) / 2.2f
        if (p > 1f) continue
        val centre = Offset((0.15f + 0.7f * h(n * 3 + b, 1)) * size.width, (0.12f + 0.3f * h(n * 3 + b, 2)) * size.height)
        val colour = CONFETTI[(n + b) % CONFETTI.size]
        val ease = 1f - (1f - p) * (1f - p)
        for (s in 0 until 26) {
            val angle = s / 26f * 2f * PI.toFloat()
            val distance = (50f + 60f * h(s + b * 40, 3)) * dp * ease
            val at = centre + Offset(cos(angle) * distance, sin(angle) * distance + 40 * dp * p * p)
            drawCircle(colour.copy(alpha = 1f - p), 2.2f * dp * (1f - p * 0.5f), at)
        }
    }
}

private fun DrawScope.pulse(t: Float) {
    val dp = density
    val centre = Offset(size.width / 2f, size.height * 0.62f)
    for (i in 0 until 3) {
        val life = (t * 0.25f + i / 3f) % 1f
        val colour = if (i % 2 == 0) AppTheme.glow else AppTheme.glow2
        val radius = life * size.width * 0.9f
        drawCircle(colour.copy(alpha = (1f - life) * 0.3f), radius, centre, style = Stroke(10 * dp))
        drawCircle(colour.copy(alpha = (1f - life) * 0.8f), radius, centre, style = Stroke(2 * dp))
    }
}

/** The points of a jagged bolt falling from ([x0], [y0]) to the height [y1]. */
private fun bolt(seed: Int, x0: Float, y0: Float, y1: Float, steps: Int, jitter: Float, drift: Float): List<Offset> {
    val points = ArrayList<Offset>()
    var x = x0
    for (k in 0..steps) {
        if (k > 0) x += (h(seed + k, 7) - 0.5f) * 2f * jitter + drift
        points.add(Offset(x, y0 + (y1 - y0) * k / steps))
    }
    return points
}

private fun DrawScope.strike(points: List<Offset>, strength: Float, width: Float) {
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (p in points.drop(1)) lineTo(p.x, p.y)
    }
    drawPath(path, AppTheme.glow2.copy(alpha = 0.28f * strength), style = Stroke(width * 5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, Color.White.copy(alpha = 0.5f * strength), style = Stroke(width * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, Color.White.copy(alpha = strength), style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/** A big bolt from the top of the screen with a branch, a flash that lights everything and a flicker, every seven seconds. */
private fun DrawScope.lightning(t: Float) {
    val dp = density
    val period = 7f
    val n = (t / period).toInt()
    val phase = t % period
    if (phase > 0.9f) return
    val strength = when {
        phase < 0.12f -> 1f
        phase < 0.2f -> 0.2f
        phase < 0.32f -> 0.9f
        else -> ((1f - (phase - 0.32f) / 0.58f).coerceIn(0f, 1f)) * 0.6f
    }
    drawRect(Color.White.copy(alpha = 0.22f * strength))
    val x0 = (0.2f + 0.6f * h(n, 1)) * size.width
    val main = bolt(n * 17, x0, -10 * dp, size.height * 0.85f, 14, 26 * dp, (h(n, 2) - 0.5f) * 10 * dp)
    strike(main, strength, 3 * dp)
    val from = main[5]
    val side = if (h(n, 3) > 0.5f) 1f else -1f
    val branch = bolt(n * 17 + 99, from.x, from.y, from.y + size.height * 0.3f, 6, 20 * dp, side * 14 * dp)
    strike(branch, strength * 0.8f, 2 * dp)
}
