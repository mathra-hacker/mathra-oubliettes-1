package app.oubliettes

import android.content.SharedPreferences
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import app.oubliettes.game.Language
import java.util.Locale

private const val LANG_KEY = "lang"

/**
 * The language of the screens: English, or Sinhala (සිංහල). It is kept in the preferences under "lang"
 * ("si" or "en"); until the player chooses, it follows the language of the phone.
 *
 * The screens are written in English. Every [Text] goes through [tr] on its way to the screen, so
 * the code never needs to know which language is shown: it compares and stores English, and the
 * Sinhala only appears at the last moment. Texts that are not drawn with [Text] (screen reader
 * names) call [tr] themselves.
 */
internal object Lang {
    /** Read by the screens: they are drawn again when it changes. */
    var si by mutableStateOf(false)
        private set

    fun load(prefs: SharedPreferences) {
        apply(prefs.getString(LANG_KEY, null)?.let { it == "si" } ?: (Locale.getDefault().language == "si"))
    }

    fun choose(prefs: SharedPreferences, sinhala: Boolean) {
        apply(sinhala)
        prefs.edit().putString(LANG_KEY, if (sinhala) "si" else "en").apply()
    }

    private fun apply(sinhala: Boolean) {
        si = sinhala
        Language.sinhala = sinhala
    }
}

private val cache = HashMap<String, String>()

/** [english] as the player reads it: itself in English, in Sinhala when there is a translation, itself when there is none. */
internal fun tr(english: String): String {
    if (!Lang.si || english.isEmpty()) return english
    if (cache.size > 4_000) cache.clear()
    return cache.getOrPut(english) { Sinhala.translate(english) }
}

/**
 * The [androidx.compose.material3.Text] of the app: the same, with its text in the language chosen.
 * The screens import nothing else, so that none of them can show English by forgetting to translate.
 */
@Composable
internal fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val sinhala = Lang.si
    androidx.compose.material3.Text(
        text = if (sinhala) tr(text) else text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        // Sinhala letters stand taller than Latin ones: a line height set for English would cut them.
        lineHeight = if (sinhala && lineHeight != TextUnit.Unspecified) lineHeight * 1.3f else lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style,
    )
}
