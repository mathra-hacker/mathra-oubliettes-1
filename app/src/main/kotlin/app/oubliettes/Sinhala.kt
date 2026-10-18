package app.oubliettes

/**
 * The Sinhala (සිංහල) of every text of the app, by its English. [translate] gives an English text its
 * Sinhala: a whole text from [WORDS] or [LONG], or one with numbers in it from [PATTERNS]. Texts made of
 * parts joined by " · " are translated a part at a time, and a leading emoji is kept.
 * A text with no translation stays as it is.
 */
internal object Sinhala {
    private val EMOJI_FIRST = Regex("^([^\\p{L}\\p{N}]+)(\\p{L}.*)$")

    fun translate(english: String): String {
        if (!english.any { it.isLetter() }) return english
        if (' ' in english && " · " in english) return english.split(" · ").joinToString(" · ") { part(it) }
        return part(english)
    }

    private fun part(text: String): String {
        WORDS[text]?.let { return it }
        LONG[text]?.let { return it }
        for ((pattern, build) in PATTERNS) pattern.matchEntire(text)?.let { return build(it.groupValues) }
        EMOJI_FIRST.matchEntire(text)?.let { match ->
            val rest = match.groupValues[2]
            val translated = part(rest)
            if (translated != rest) return match.groupValues[1] + translated
        }
        return text
    }

    /** Texts with numbers in them. */
    private val PATTERNS: List<Pair<Regex, (List<String>) -> String>> = listOf(
        Regex("Lv (\\d+)") to { g -> "මට්ටම ${g[1]}" },
        Regex("Level (\\d+)") to { g -> "මට්ටම ${g[1]}" },
        Regex("(\\d+) / (\\d+) solved") to { g -> "${g[1]} / ${g[2]} විසඳා ඇත" },
        Regex("(\\d+) solved") to { g -> "විසඳූ ${g[1]}ක්" },
        Regex("(\\d+) / (\\d+) mapped") to { g -> "${g[1]} / ${g[2]} සිතියම්ගත කර ඇත" },
        Regex("(\\d+) daily dungeons mapped") to { g -> "සිතියම්ගත කළ දෛනික සිරගෙවල් ${g[1]}ක්" },
        Regex("(\\d+) dungeons mapped") to { g -> "සිතියම්ගත කළ සිරගෙවල් ${g[1]}ක්" },
        Regex("(\\d+) / 100 XP to level (\\d+)") to { g -> "මට්ටම ${g[2]} දක්වා XP ${g[1]} / 100" },
        Regex("Step (\\d+) / (\\d+)") to { g -> "පියවර ${g[1]} / ${g[2]}" },
        Regex("Your turn: (\\d+) to mark") to { g -> "ඔබේ වාරය: සලකුණු කළ යුතු ${g[1]}ක්" },
        Regex("(\\d+) levels") to { g -> "මට්ටම් ${g[1]}ක්" },
        Regex("(\\d+) themes") to { g -> "තේමා ${g[1]}ක්" },
        Regex("(\\d+) effects") to { g -> "එෆෙක්ට් ${g[1]}ක්" },
        Regex("(\\d+) mistakes, crossed out\\.") to { g -> "වැරදි ${g[1]}ක්, ඉරි ගසා ඇත." },
        Regex("(\\d+) of (\\d+) difficulties completed") to { g -> "දුෂ්කරතා මට්ටම් ${g[2]}න් ${g[1]}ක් සම්පූර්ණයි" },
        Regex("Dungeon, (\\d+) by (\\d+)") to { g -> "සිරගෙය, ${g[1]} × ${g[2]}" },
        Regex("Column (\\d+): (\\d+) walls, (\\d+) marked") to { g -> "තීරුව ${g[1]}: බිත්ති ${g[2]}ක්, සලකුණු කළ ${g[3]}ක්" },
        Regex("Row (\\d+): (\\d+) walls, (\\d+) marked") to { g -> "පේළිය ${g[1]}: බිත්ති ${g[2]}ක්, සලකුණු කළ ${g[3]}ක්" },
        Regex("Row (\\d+), column (\\d+): (.*)") to { g ->
            "පේළිය ${g[1]}, තීරුව ${g[2]}: " + g[3].split(", ").joinToString(", ") { CELL[it] ?: it }
        },
    )

    /** What a cell is called to a screen reader. */
    private val CELL = mapOf(
        "monster" to "රාක්ෂයා", "chest" to "නිධි පෙට්ටිය", "wall" to "බිත්තිය", "open" to "විවෘත", "unknown" to "නොදන්නා",
        "to mark" to "සලකුණු කළ යුතුය", "mistake" to "වැරදියි", "tried" to "උත්සාහ කළා",
    )

    /** Names, labels and short lines. */
    private val WORDS: Map<String, String> = mapOf(
        // Menu
        "Solved" to "විසඳූ", "Daily" to "දෛනික", "Chapters" to "පරිච්ඡේද", "Dailies" to "දෛනික",
        "Done" to "අවසන්", "Ready" to "සූදානම්",
        "Tutorial" to "පුහුණුව", "Continue" to "දිගටම කරන්න", "Campaign" to "කතා මාදිලිය", "Endless" to "නිමක් නැති",
        "Options" to "සැකසුම්", "Quit" to "පිටවීම", "About" to "ගැන", "Menu" to "මෙනුව", "Player" to "ක්‍රීඩකයා",
        "FX on" to "එෆෙක්ට් සක්‍රියයි", "FX off" to "එෆෙක්ට් අක්‍රියයි",
        // Campaign and Endless
        "Levels" to "මට්ටම්", "Next level" to "ඊළඟ මට්ටම", "Choose" to "තෝරන්න", "Size" to "ප්‍රමාණය", "Reasoning" to "තර්ක මට්ටම",
        "Enter" to "ඇතුළු වන්න", "Gallery" to "ගැලරිය", "Enter a code" to "කේතයක් ඇතුළත් කරන්න",
        "Next grid" to "ඊළඟ ජාලය", "New grid" to "අලුත් ජාලය", "Dig another" to "තවත් එකක් හාරන්න",
        "Digging…" to "හාරමින්…", "The diggers gave up on this one." to "හාරන්නන් මෙය අත්හැරියා.",
        "Shared" to "බෙදාගත්", "No dungeon here." to "මෙහි සිරගෙයක් නැත.", "A friend's dungeon" to "මිතුරෙකුගේ සිරගෙය",
        "from a friend" to "මිතුරෙකුගෙන්", "daily dungeons" to "දෛනික සිරගෙවල්",
        "Paste what a friend shared from a solved grid: you get the same dungeon." to
            "මිතුරෙක් විසඳූ ජාලයකින් බෙදාගත් දේ මෙහි අලවන්න: ඔබට ලැබෙන්නේ එම සිරගෙයමයි.",
        "Code" to "කේතය", "Dig" to "හාරන්න", "Cancel" to "අවලංගු කරන්න",
        "No dungeon in there." to "ඒ තුළ සිරගෙයක් නැත.",
        "This one cannot be solved by reasoning alone." to "මෙය තර්කයෙන් පමණක් විසඳිය නොහැක.",
        "Dungeons solved in Endless, Daily or from a friend are kept here." to
            "නිමක් නැති, දෛනික හෝ මිතුරෙකුගෙන් විසඳූ සිරගෙවල් මෙහි තබා ඇත.",
        "not started" to "පටන් ගෙන නැත", "begun" to "පටන් ගත්තා", "solved" to "විසඳා ඇත", "completed" to "සම්පූර්ණයි",
        // Difficulties and reasoning
        "Novice" to "නවකයා", "Easy" to "පහසු", "Medium" to "මධ්‍යම", "Hard" to "අමාරු", "Leeroy" to "ලීරෝයි",
        "Gentle" to "මෘදු", "Tricky" to "උපක්‍රමශීලී", "Fiendish" to "දරුණු",
        // Chapters
        "The Gatehouse" to "දොරටු මන්දිරය", "The Guardroom" to "මුරකාර කාමරය", "The Kennels" to "බල්ලන්ගේ කූඩු",
        "The Kitchens" to "මුළුතැන්ගෙවල්", "The Wine Cellar" to "වයින් ගබඩාව",
        "The Dungeons" to "සිරගෙවල්", "The Cells" to "සිරකුටි", "The Well" to "ළිඳ", "The Old Sewers" to "පැරණි අපවාහන මාර්ග",
        "The Rat Warren" to "මීයන්ගේ බෙනහාලු",
        "The Crypts" to "භූගත සොහොන්", "The Ossuary" to "ඇටකටු ගබඩාව", "The Chapel Below" to "පහළ දේවස්ථානය",
        "The Tombs of the Kings" to "රජුන්ගේ සොහොන්", "The Sealed Vault" to "මුද්‍රා තැබූ සුරක්ෂිතාගාරය",
        "The King's Stores" to "රජුගේ ගබඩා", "The Armoury" to "ආයුධාගාරය", "The Treasury" to "භාණ්ඩාගාරය",
        "The Counting House" to "ගණන් කරන මන්දිරය", "The Hidden Hoard" to "සැඟවුණු නිධානය",
        "The Forgotten Maze" to "අමතක වූ මායා මාවත", "The Sunken Halls" to "ගිලී ගිය ශාලා",
        "The Roots of the Keep" to "බලකොටුවේ මුල්", "The Black Pit" to "කළු වළ", "The Oubliette" to "අඳුරු සිරකුටිය",
        // Account
        "Account" to "ගිණුම", "Avatar" to "අවතාර් රූපය", "Badges" to "ලාංඡන", "Edit name" to "නම වෙනස් කරන්න",
        "Backup & settings" to "උපස්ථ සහ සැකසුම්", "Your name" to "ඔබේ නම", "Save" to "සුරකින්න",
        "Rookie" to "නවක", "Digger" to "හාරන්නා", "Delver" to "ගවේෂකයා", "Trapfinder" to "උගුල් සොයන්නා",
        "Dungeon Master" to "සිරගෙයි ස්වාමියා", "Vault Keeper" to "සුරක්ෂිතාගාර රකින්නා", "Legend" to "ජනප්‍රවාදය",
        "Oubliette King" to "අඳුරු සිරකුටි රජු",
        "First dungeon" to "පළමු සිරගෙය", "Daily done" to "දෛනික අවසන්", "7 dailies" to "දෛනික 7ක්",
        "Chapter done" to "පරිච්ඡේදයක් අවසන්", "All chapters" to "සියලු පරිච්ඡේද",
        // About
        "About the game" to "ක්‍රීඩාව ගැන", "Dungeon logic puzzle" to "සිරගෙයි තර්ක ප්‍රහේලිකාව", "Backup" to "උපස්ථය",
        "Developer" to "නිර්මාණකරු", "Spin" to "කරකවන්න", "Share" to "බෙදාගන්න", "Follow and contact" to "අනුගමනය කර සම්බන්ධ වන්න",
        "Call" to "අමතන්න",
        "Work out where the dungeon walls are from the clues on the edge of the grid. Every grid has exactly one " +
            "solution: find the monsters' dead ends, the treasure rooms and the hidden hallways, one mark at a time." to
            "ජාලයේ දාරයේ ඇති ඉඟි අනුව සිරගෙයේ බිත්ති තිබෙන්නේ කොතැනදැයි සොයා ගන්න. සෑම ජාලයකටම හරියටම එක් විසඳුමක් ඇත: " +
            "රාක්ෂයින්ගේ මළ කෙළවර, නිධි කාමර සහ සැඟවුණු කොරිඩෝ සොයා ගන්න, එක් සලකුණක් බැගින්.",
        "Based on the open-source game Oubliettes by all3f0r1, under the GNU GPL v3. " +
            "Fonts: Almendra and UnifrakturCook (SIL OFL)." to
            "විවෘත මූලාශ්‍ර ක්‍රීඩාව වන all3f0r1 ගේ Oubliettes මත පදනම්ව, GNU GPL v3 යටතේ. " +
            "අකුරු: Almendra සහ UnifrakturCook (SIL OFL).",
        // Options
        "Theme" to "තේමාව", "Dashboard effects" to "ප්‍රධාන තිරයේ එෆෙක්ට්", "All on" to "සියල්ල සක්‍රිය", "All off" to "සියල්ල අක්‍රිය",
        "Sound and play" to "ශබ්ද සහ ක්‍රීඩාව", "Sounds" to "ශබ්ද", "Music" to "සංගීතය", "Progress" to "ප්‍රගතිය",
        "Save to a file" to "ගොනුවකට සුරකින්න", "Load a file" to "ගොනුවක් පූරණය කරන්න", "More" to "තවත්",
        "Reset everything" to "සියල්ල යළි පිහිටුවන්න", "Keep" to "තබා ගන්න", "Keep mine" to "මගේ ඒවා තබා ගන්න", "Load" to "පූරණය කරන්න",
        "Language" to "භාෂාව",
        "Background photo" to "පසුබිම් ඡායාරූපය", "Choose a photo" to "ඡායාරූපයක් තෝරන්න", "Remove" to "ඉවත් කරන්න",
        "Photo dimming" to "ඡායාරූපය අඳුරු කිරීම", "This photo could not be used." to "මෙම ඡායාරූපය භාවිතා කළ නොහැක.",
        "Pick a photo from your gallery: it shows behind every screen." to "ඔබේ ගැලරියෙන් ඡායාරූපයක් තෝරන්න: එය සෑම තිරයක් පිටුපසම පෙනේ.",
        "Progress saved to the file." to "ප්‍රගතිය ගොනුවට සුරැකුණා.",
        "The file could not be written." to "ගොනුව ලිවීමට නොහැකි විය.",
        "This file is not a Mathra Oubliettes save." to "මෙම ගොනුව Mathra Oubliettes සුරැකුමක් නොවේ.",
        "Shown on the main screen and while playing. Nothing moves in calm mode." to
            "පෙන්වන්නේ ප්‍රධාන තිරයේ සහ සෙල්ලම් කරන අතරතුරයි. සන්සුන් ප්‍රකාරයේදී කිසිවක් නොසෙලවෙයි.",
        "Vibrate on every mark" to "සෑම සලකුණකටම කම්පනය කරන්න",
        "Keep the screen on while playing" to "සෙල්ලම් කරන අතරතුර තිරය නොනිවා තබන්න",
        "Finish a row or a column once it can only be filled one way" to
            "පේළියක් හෝ තීරුවක් පිරවිය හැක්කේ එක් ආකාරයකට පමණක් වූ විට එය සම්පූර්ණ කරන්න",
        "Say when every count is met but a rule is broken" to "සියලු ගණන් සපුරා ඇතත් නීතියක් කඩ වී ඇති විට දන්වන්න",
        "Hints: a Hint plank under the grid says where to look, then why, then which cells" to
            "ඉඟි: ජාලය යට ඇති ඉඟි බොත්තමෙන් මුලින් බැලිය යුතු තැන, ඉන්පසු හේතුව, ඉන්පසු කොටු පෙන්වයි",
        "Endless: on to the next grid by itself once one is solved" to
            "නිමක් නැති මාදිලිය: එකක් විසඳූ පසු ඊළඟ ජාලයට ස්වයංක්‍රීයව යන්න",
        "Help: a Check plank under the grid shows the wrong marks for three seconds, once a minute" to
            "උදව්: ජාලය යට ඇති පරීක්ෂා බොත්තමෙන් මිනිත්තුවකට වරක්, වැරදි සලකුණු තත්පර තුනක් පෙන්වයි",
        "Magnifier under the finger on large grids" to "විශාල ජාලවල ඇඟිල්ල යට විශාලනය",
        "Plain digits for the wall counts (or tap a count on the grid)" to
            "බිත්ති ගණන් සඳහා සරල ඉලක්කම් (නැතහොත් ජාලයේ ගණනක් තට්ටු කරන්න)",
        "Calm mode: nothing moves on its own" to "සන්සුන් ප්‍රකාරය: කිසිවක් තනිවම නොසෙලවෙයි",
        "Progress stays through updates, but not when the app is removed. A file keeps it: " +
            "solved levels, grids in progress and settings." to
            "යාවත්කාලීන කිරීම්වලදී ප්‍රගතිය රැඳේ, නමුත් ඇප් එක ඉවත් කළ විට නොරැඳේ. ගොනුවක් එය තබා ගනී: " +
            "විසඳූ මට්ටම්, සෙල්ලම් කරමින් සිටින ජාල සහ සැකසුම්.",
        "Erase all progress, the account and the settings on this phone? Save to a file first to keep a copy." to
            "මෙම දුරකථනයේ සියලු ප්‍රගතිය, ගිණුම සහ සැකසුම් මකා දමනවාද? පිටපතක් තබා ගැනීමට මුලින් ගොනුවකට සුරකින්න.",
        "Replace the progress and the settings on this phone by those of the file?" to
            "මෙම දුරකථනයේ ප්‍රගතිය සහ සැකසුම් ගොනුවේ ඒවායින් ප්‍රතිස්ථාපනය කරනවාද?",
        // Themes and effects
        "Teddy Gold" to "ටෙඩි රන්වන්", "Neon Blue" to "නියෝන් නිල්", "Emerald" to "මරකත", "Crimson" to "තද රතු",
        "Royal Violet" to "රාජකීය දම්", "Sunset" to "හිරු බැසීම", "Midnight" to "මැදියම", "Candy" to "රසකැවිලි",
        "Lightning" to "අකුණු", "Rain" to "වැස්ස", "Snow" to "හිම", "Stars" to "තරු", "Meteors" to "උල්කාපාත",
        "Fireflies" to "කණාමැදිරි", "Embers" to "ගිනි අඟුරු", "Fog" to "මීදුම", "Aurora" to "අරෝරා", "Bubbles" to "බුබුළු",
        "Petals" to "මල් පෙති", "Digital rain" to "ඩිජිටල් වැස්ස", "Confetti" to "කොන්ෆෙටි", "Fireworks" to "ගිනිකෙළි",
        "Neon rings" to "නියෝන් වළලු",
        // In the game
        "Wall" to "බිත්තිය", "Open" to "විවෘත", "Unknown" to "නොදන්නා", "change" to "වෙනස් කරන්න", "Hallway" to "කොරිඩෝව",
        "Rules" to "නීති", "Close" to "වසන්න", "Checkpoint" to "චෙක්පොයින්ට්", "Return" to "ආපසු යන්න", "Reset" to "යළි පිහිටුවන්න",
        "Change theme" to "තේමාව වෙනස් කරන්න", "Play again" to "නැවත සෙල්ලම් කරන්න", "Undo" to "අහෝසි කරන්න", "Redo" to "නැවත කරන්න",
        "Hint" to "ඉඟිය", "Why?" to "ඇයි?", "Show me" to "පෙන්වන්න", "Check" to "පරීක්ෂා කරන්න",
        "Dungeon solved!" to "සිරගෙය විසඳුවා!", "No mistake so far." to "මෙතෙක් වැරදි නැත.",
        "1 mistake, crossed out." to "වැරද්දක් ඇත, ඉරි ගසා ඇත.",
        "A mark is wrong: no hint can build on it." to "සලකුණක් වැරදියි: ඊට පදනම් වී ඉඟියක් දිය නොහැක.",
        "Every count is met, yet a rule is broken." to "සියලු ගණන් සපුරා ඇත, නමුත් නීතියක් කඩ වී ඇත.",
        "Leave this grid? Its marks will be lost." to "මෙම ජාලය හැර යනවාද? එහි සලකුණු නැති වී යයි.",
        "Empty this grid? Undo brings the marks back." to "මෙම ජාලය හිස් කරනවාද? අහෝසි කිරීමෙන් සලකුණු ආපසු ලැබේ.",
        "Stay" to "සිටින්න",
        // Lessons
        "The basics" to "මූලික කරුණු", "Counts, monsters, checkpoints" to "ගණන්, රාක්ෂයින්, චෙක්පොයින්ට්",
        "Play" to "සෙල්ලම් කරන්න", "Practise" to "පුහුණු වන්න", "Lessons" to "පාඩම්",
        "On your own" to "ඔබ විසින්ම", "Lesson learnt" to "පාඩම ඉගෙන ගත්තා", "Previous" to "පෙර", "Next" to "ඊළඟ",
        "Treasure rooms" to "නිධි කාමර", "Where the room is, and its exit" to "කාමරය කොතැනද, එහි පිටවීම",
        "Dead ends" to "මළ කෙළවර", "No dead end without its monster" to "රාක්ෂයා නැති මළ කෙළවරක් නැත",
        "Narrow hallways" to "පටු කොරිඩෝ", "No open 2×2 block" to "විවෘත 2×2 කොටසක් නැත",
        "One dungeon" to "එකම සිරගෙය", "Nothing is cut off" to "කිසිවක් වෙන් වී නැත",
        "Trying it out" to "උත්සාහ කර බැලීම", "When no rule gives a cell" to "කිසිම නීතියක් කොටුවක් නොදෙන විට",
    )

    /** Longer texts: the rules, the tutorial and the lessons. */
    private val LONG: Map<String, String> = mapOf(
        "The numbers give how many walls their row or column holds." to
            "අංකවලින් පෙන්වන්නේ ඒ පේළියේ හෝ තීරුවේ බිත්ති කීයක් තිබේද යන්නයි.",
        "A number is dimmed and struck through once its count is reached, which does not prove the walls " +
            "are the right ones. It turns red and underlined when there are too many." to
            "අංකයක ගණන සම්පූර්ණ වූ විට එය අඳුරු වී ඉරක් වැටේ; එයින් බිත්ති නිවැරදි තැන්වල ඇති බව ඔප්පු නොවේ. " +
            "බිත්ති වැඩි වුවහොත් එය රතු පාට වී යටින් ඉරක් වැටේ.",
        "Monsters and chests are never walls." to "රාක්ෂයින් සහ නිධි පෙට්ටි කිසිදා බිත්ති නොවේ.",
        "Every monster sits in a dead end, and every dead end holds a monster." to
            "සෑම රාක්ෂයෙක්ම මළ කෙළවරක සිටී; සෑම මළ කෙළවරකම රාක්ෂයෙක් සිටී.",
        "Every chest is in a treasure room: 3×3 open cells, a single chest (anywhere in the room) and a single opening." to
            "සෑම නිධි පෙට්ටියක්ම ඇත්තේ නිධි කාමරයක: විවෘත කොටු 3×3ක්, එක් පෙට්ටියක් (කාමරයේ ඕනෑම තැනක) සහ එක් විවරයක්.",
        "Outside treasure rooms, hallways are one cell wide: no open 2×2 block." to
            "නිධි කාමරවලින් පිටත, කොරිඩෝ පළලින් එක් කොටුවකි: විවෘත 2×2 කොටසක් නොතිබිය යුතුය.",
        "All open cells are connected." to "සියලුම විවෘත කොටු එකිනෙක සම්බන්ධ වී තිබිය යුතුය.",
        "Walls are never more than two cells thick: no 3×3 block of walls." to
            "බිත්ති කොටු දෙකකට වඩා ඝන නොවේ: බිත්තිවලින් 3×3 කොටසක් නොතිබිය යුතුය.",
        "Tap a cell to change it: wall, hallway (a dot: known open), blank. Pick Wall or Hallway under the grid to lay " +
            "that mark at the first tap. Drag to fill a row or a column with what the first cell became; a " +
            "drag only fills blank cells and those of your previous action." to
            "කොටුවක් තට්ටු කර වෙනස් කරන්න: බිත්තිය, කොරිඩෝව (තිතක්: විවෘත බව දනී), හිස්. පළමු තට්ටුවේදීම අදාළ සලකුණ දැමීමට " +
            "ජාලය යට ඇති බිත්තිය හෝ කොරිඩෝව තෝරන්න. පළමු කොටුව බවට පත් වූ දෙයින් පේළියක් හෝ තීරුවක් පිරවීමට ඇදගෙන යන්න; " +
            "ඇදගෙන යාමෙන් පිරවෙන්නේ හිස් කොටු සහ ඔබේ කලින් ක්‍රියාවේ කොටු පමණි.",
        "When deduction runs dry and you must try something out, lay a Checkpoint first: Return brings " +
            "the grid back to it if the try leads nowhere. What was laid since bears a parchment corner." to
            "අනුමාන කිරීමට දෙයක් නැති වී යමක් උත්සාහ කර බැලිය යුතු වූ විට, මුලින් චෙක්පොයින්ට් එකක් තබන්න: උත්සාහය අසාර්ථක වුවහොත් " +
            "ආපසු යාමෙන් ජාලය එතැනට ගෙන එයි. ඉන්පසු දැමූ දේවල් කොනක පැරැන්පත් සලකුණක් දරයි.",
        "On a grid with small cells, spread two fingers to see it larger, and move it with both. Tap a count for plain digits." to
            "කුඩා කොටු සහිත ජාලයක, ඇඟිලි දෙකක් විහිදුවා විශාල කර බලන්න, ඒ ඇඟිලි දෙකෙන්ම එය ගෙන යන්න. සරල ඉලක්කම් සඳහා ගණනක් තට්ටු කරන්න.",
        // Tutorial
        "The goal: find every wall of the dungeon. Each number tells how many walls its row or " +
            "column holds. Monsters and chests are never walls." to
            "ඉලක්කය: සිරගෙයේ සෑම බිත්තියක්ම සොයා ගැනීමයි. සෑම අංකයකින්ම කියන්නේ ඒ පේළියේ හෝ තීරුවේ බිත්ති කීයක් තිබේද කියායි. " +
            "රාක්ෂයින් සහ නිධි පෙට්ටි කිසිදා බිත්ති නොවේ.",
        "The last row and the last column ask for 6 walls in 6 cells: all of them are walls. " +
            "Tap a cell to build a wall, or drag along the line to build them all." to
            "අවසාන පේළිය සහ අවසාන තීරුව කොටු 6ක බිත්ති 6ක් ඉල්ලයි: ඒ සියල්ලම බිත්ති. බිත්තියක් ගොඩනැගීමට කොටුවක් තට්ටු කරන්න, " +
            "නැතහොත් සියල්ල ගොඩනැගීමට රේඛාව දිගේ ඇදගෙන යන්න.",
        "Rows 3 and 5 ask for a single wall, already placed on the right: their other cells are " +
            "open. Pick Hallway under the grid, then tap them or drag along the row. Without it, a " +
            "second tap turns a wall into a hallway." to
            "3 සහ 5 පේළි ඉල්ලන්නේ එක් බිත්තියක් පමණි, එය දකුණු පැත්තේ දැනටමත් තබා ඇත: ඒවායේ අනෙක් කොටු විවෘතයි. " +
            "ජාලය යට කොරිඩෝව තෝරා, ඒවා තට්ටු කරන්න, නැතහොත් පේළිය දිගේ ඇදගෙන යන්න. එය නොතෝරා නම්, දෙවන තට්ටුවෙන් බිත්තියක් කොරිඩෝවක් බවට පත් වේ.",
        "A monster lives at the end of a dead end: it has exactly one open cell next to it. " +
            "This one already has one below, so its other neighbours are walls." to
            "රාක්ෂයෙක් ජීවත් වන්නේ මළ කෙළවරක අග: ඔහු අසල හරියටම එක් විවෘත කොටුවක් ඇත. " +
            "මොහුට දැනටමත් පහළින් එකක් ඇත, එබැවින් ඔහුගේ අනෙක් අසල්වැසි කොටු බිත්ති වේ.",
        "On harder grids deduction may run dry, and something has to be tried out. Lay a Checkpoint " +
            "first: Return brings the grid back to it if the try leads nowhere. Try it: Checkpoint, " +
            "build a wall anywhere, then Return." to
            "අමාරු ජාලවල අනුමාන කිරීමට දෙයක් නැති වී යමක් උත්සාහ කර බැලිය යුතු විය හැක. මුලින් චෙක්පොයින්ට් එකක් තබන්න: " +
            "උත්සාහය අසාර්ථක වුවහොත් ආපසු යාමෙන් ජාලය එතැනට ගෙන එයි. උත්සාහ කර බලන්න: චෙක්පොයින්ට්, ඕනෑම තැනක බිත්තියක් ගොඩනඟන්න, ඉන්පසු ආපසු යන්න.",
        "Column 5 has its 2 walls: its last unknown cell is open. Row 4 then asks for 5 walls " +
            "and has only 5 possible cells left: all of them are walls." to
            "5 වන තීරුවේ බිත්ති 2ම ඇත: එහි අවසාන නොදන්නා කොටුව විවෘතයි. එවිට 4 වන පේළිය බිත්ති 5ක් ඉල්ලයි, " +
            "ඉතිරි විය හැකි කොටු 5ක් පමණක් ඇත: ඒ සියල්ලම බිත්ති.",
        "Column 4 asks for 4 walls. It has 3 and a single unknown cell: that is the fourth." to
            "4 වන තීරුව බිත්ති 4ක් ඉල්ලයි. එහි 3ක් ඇත, නොදන්නා කොටුව එකක් පමණි: එය සිව්වැන්නයි.",
        "Rows 1 and 2 now have all their walls: what is left is open." to
            "1 සහ 2 පේළිවල දැන් සියලු බිත්ති ඇත: ඉතිරි ඒවා විවෘතයි.",
        "The chest is in a treasure room: 3×3 open cells, a single chest (anywhere in the room) " +
            "and a single exit. Everywhere else hallways are one cell wide, with no open 2×2 block, " +
            "and the whole dungeon is connected. Dungeon solved!" to
            "නිධි පෙට්ටිය ඇත්තේ නිධි කාමරයක: විවෘත කොටු 3×3ක්, එක් පෙට්ටියක් (කාමරයේ ඕනෑම තැනක) සහ එක් පිටවීමක්. " +
            "අනෙක් හැම තැනකම කොරිඩෝ පළලින් එක් කොටුවකි, විවෘත 2×2 කොටසක් නැත, සමස්ත සිරගෙයම සම්බන්ධයි. සිරගෙය විසඳුවා!",
        "Dungeon solved! Now one on your own: the Hint plank is there if you get stuck." to
            "සිරගෙය විසඳුවා! දැන් ඔබම එකක් කරන්න: ඔබ හිර වුවහොත් ඉඟි බොත්තම ඇත.",
        // Lessons
        "A chest sits in a treasure room: 3×3 open cells with a single exit. The room may lie several " +
            "ways around its chest, and the cells it covers every way are open for sure. Once it is " +
            "placed, all the cells around it are walls but one." to
            "නිධි පෙට්ටියක් ඇත්තේ නිධි කාමරයක: එක් පිටවීමක් සහිත විවෘත කොටු 3×3ක්. කාමරය පෙට්ටිය වටා විවිධ ආකාරවලින් පිහිටිය හැක, " +
            "එහෙත් හැම ආකාරයකටම ආවරණය වන කොටු නියත වශයෙන්ම විවෘතයි. කාමරය තැබූ පසු, එය වටා ඇති කොටු සියල්ලම බිත්ති වේ, එකක් හැර.",
        "Every dead end holds a monster. So a cell with no monster and a single way out cannot be " +
            "open: it is a wall. And an open cell needs two ways out: when just two are left, both are open." to
            "සෑම මළ කෙළවරකම රාක්ෂයෙක් සිටී. එබැවින් රාක්ෂයෙකු නැති, පිටවීමට එක් මාර්ගයක් පමණක් ඇති කොටුවක් විවෘත විය නොහැක: එය බිත්තියකි. " +
            "විවෘත කොටුවකට පිටවීමේ මාර්ග දෙකක් අවශ්‍යයි: ඉතිරිව ඇත්තේ දෙකක් පමණක් නම්, දෙකම විවෘතයි.",
        "Outside treasure rooms, hallways are one cell wide: four cells in a square are never all " +
            "open. Where three of them are, the fourth is a wall." to
            "නිධි කාමරවලින් පිටත, කොරිඩෝ පළලින් එක් කොටුවකි: හතරැස් හැඩයට ඇති කොටු හතර කිසිදා සියල්ලම විවෘත නොවේ. " +
            "ඉන් තුනක් විවෘත නම්, සිව්වැන්න බිත්තියකි.",
        "All open cells are connected: any monster could walk to any other. When a single cell joins " +
            "two parts of the dungeon, it cannot be a wall. To find it, picture a wall there and see what is cut off." to
            "සියලුම විවෘත කොටු සම්බන්ධයි: ඕනෑම රාක්ෂයෙකුට අනෙක් ඕනෑම කෙනෙකු වෙත ඇවිදගෙන යා හැක. තනි කොටුවක් සිරගෙයේ කොටස් දෙකක් " +
            "සම්බන්ධ කරන්නේ නම්, එය බිත්තියක් විය නොහැක. එය සොයා ගැනීමට, එහි බිත්තියක් ඇතැයි සිතා කුමක් වෙන් වේදැයි බලන්න.",
        "When no rule gives a cell, suppose one: a wall here, say. Follow what that forces: if a rule " +
            "breaks, the cell was open after all. On your own grids, lay a Checkpoint before the try " +
            "and Return once it has failed." to
            "කිසිම නීතියක් කොටුවක් නොදෙන විට, එකක් අනුමාන කරන්න: උදාහරණයක් ලෙස මෙහි බිත්තියක්. එයින් සිදුවන දේ අනුගමනය කරන්න: " +
            "නීතියක් කඩ වුවහොත්, කොටුව ඇත්තටම විවෘතයි. ඔබේ ජාලවලදී, උත්සාහයට පෙර චෙක්පොයින්ට් එකක් තබා, එය අසාර්ථක වූ පසු ආපසු යන්න.",
    )
}
