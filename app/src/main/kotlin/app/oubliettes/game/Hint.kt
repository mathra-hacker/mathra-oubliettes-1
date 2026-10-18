package app.oubliettes.game

private val sinhala get() = Language.sinhala

private fun Puzzle.at(cell: Int) =
    if (sinhala) "පේළිය ${cell / width + 1}, තීරුව ${cell % width + 1}" else "row ${cell / width + 1}, column ${cell % width + 1}"

private fun Puzzle.lineName(line: Int) = when {
    sinhala -> if (line < height) "පේළිය ${line + 1}" else "තීරුව ${line - height + 1}"
    else -> if (line < height) "row ${line + 1}" else "column ${line - height + 1}"
}

/** The first word of a hint: where to look on [p], and no more. */
fun Step.nudge(p: Puzzle): String = if (sinhala) nudgeInSinhala(p) else when (rule) {
    Rule.COUNT -> "Look at ${p.lineName(line)}."
    Rule.MONSTER -> "Look at the monster in ${p.at(from[0])}."
    Rule.DEAD_END -> "Could the cell in ${p.at(from[0])} be open?"
    Rule.PASSAGE -> "Look at the open cell in ${p.at(from[0])}: how does one leave it?"
    Rule.BLOCK -> "Look at the 2×2 block whose top left is in ${p.at(from[0])}."
    Rule.ROOM -> "Where can the treasure room of the chest in ${p.at(from[0])} be?"
    Rule.EXIT -> "Look around the treasure room whose top left is in ${p.at(from[0])}."
    Rule.CUT -> "Is every cell still within reach of the rest of the dungeon?"
    Rule.TRY, Rule.EITHER -> "No rule gives a cell as the grid stands. Lay a Checkpoint and try the cell in ${p.at(from[0])}."
}

private fun Step.nudgeInSinhala(p: Puzzle) = when (rule) {
    Rule.COUNT -> "${p.lineName(line)} දෙස බලන්න."
    Rule.MONSTER -> "${p.at(from[0])} හි ඇති රාක්ෂයා දෙස බලන්න."
    Rule.DEAD_END -> "${p.at(from[0])} හි ඇති කොටුව විවෘත විය හැකිද?"
    Rule.PASSAGE -> "${p.at(from[0])} හි ඇති විවෘත කොටුව දෙස බලන්න: එයින් පිටවන්නේ කෙසේද?"
    Rule.BLOCK -> "ඉහළ-වම කොන ${p.at(from[0])} හි ඇති 2×2 කොටස දෙස බලන්න."
    Rule.ROOM -> "${p.at(from[0])} හි ඇති නිධි පෙට්ටියේ නිධි කාමරය තිබිය හැක්කේ කොතැනද?"
    Rule.EXIT -> "ඉහළ-වම කොන ${p.at(from[0])} හි ඇති නිධි කාමරය අවට බලන්න."
    Rule.CUT -> "සෑම කොටුවකටම තවමත් සිරගෙයේ ඉතිරි කොටසට ළඟා විය හැකිද?"
    Rule.TRY, Rule.EITHER -> "දැනට කිසිම නීතියක් කොටුවක් නොදෙයි. චෙක්පොයින්ට් එකක් තබා ${p.at(from[0])} හි ඇති කොටුව උත්සාහ කර බලන්න."
}

/** The second word of a hint: the rule at work on [p], and what it gives. */
fun Step.reason(p: Puzzle): String = if (sinhala) reasonInSinhala(p) else reasonInEnglish(p)

private fun Step.reasonInEnglish(p: Puzzle): String {
    val line = if (rule == Rule.COUNT) p.lineName(this.line).replaceFirstChar { it.uppercase() } else ""
    val count = if (rule == Rule.COUNT) (p.rowCounts + p.colCounts)[this.line] else 0
    return when (rule) {
        Rule.COUNT ->
            if (walls.isEmpty()) "$line has its $count walls: its other cells are open."
            else "$line asks for $count walls and has just enough cells left: they are all walls."
        Rule.MONSTER ->
            if (open.isEmpty()) "A monster has exactly one way out, and this one has it: its other neighbours are walls."
            else "A monster has exactly one way out, and this one has a single cell left for it: that cell is open."
        Rule.DEAD_END -> "Open, the cell in ${p.at(from[0])} would be a dead end, and it holds no monster: it is a wall."
        Rule.PASSAGE -> "Only a monster sits in a dead end: the cell in ${p.at(from[0])} needs two ways out, and has just two left. Both are open."
        Rule.BLOCK -> "Hallways are one cell wide: no open 2×2 block. Three cells of this one are open, so the fourth is a wall."
        Rule.ROOM -> "Wherever the 3×3 treasure room of this chest still fits, it covers the same cells: they are open."
        Rule.EXIT ->
            if (open.isEmpty()) "A treasure room has a single exit, and this one has it: every other cell around it is a wall."
            else "A treasure room has a single exit, and this one has a single cell left for it: that cell is open."
        Rule.CUT -> "All open cells are connected. Walls cut some cells off from the rest of the dungeon: they are walls too."
        Rule.TRY -> {
            val (tried, proved) = if (assumes) "a wall" to "open" else "open" to "a wall"
            val end = when (breaks) {
                Rule.COUNT -> "a row or a column ends up with the wrong number of walls"
                Rule.MONSTER -> "a monster ends up with no way out, or with two"
                Rule.BLOCK -> "an open 2×2 block appears"
                Rule.ROOM, Rule.EXIT -> "a treasure room no longer fits, or has no single exit"
                Rule.CUT -> "part of the dungeon is cut off from the rest"
                else -> "a dead end appears where no monster is"
            }
            "Suppose the cell in ${p.at(from[0])} is $tried: follow what that forces, and $end. So it is $proved."
        }
        Rule.EITHER -> "Try the cell in ${p.at(from[0])} both ways: wall or open, some cells come out the same. Those are sure."
    }
}

private fun Step.reasonInSinhala(p: Puzzle): String {
    val line = if (rule == Rule.COUNT) p.lineName(this.line) else ""
    val count = if (rule == Rule.COUNT) (p.rowCounts + p.colCounts)[this.line] else 0
    return when (rule) {
        Rule.COUNT ->
            if (walls.isEmpty()) "$line හි බිත්ති $count ම ඇත: එහි අනෙක් කොටු විවෘතයි."
            else "$line ට බිත්ති $count ක් අවශ්‍යයි, ඉතිරි කොටු ඇත්තේ හරියටම ඒවාට ප්‍රමාණවත් තරම් පමණි: ඒ සියල්ලම බිත්ති."
        Rule.MONSTER ->
            if (open.isEmpty()) "රාක්ෂයෙකුට පිටවීමට ඇත්තේ හරියටම එක් මාර්ගයකි, මොහුට එය ඇත: ඔහුගේ අනෙක් අසල්වැසි කොටු බිත්ති."
            else "රාක්ෂයෙකුට පිටවීමට ඇත්තේ හරියටම එක් මාර්ගයකි, මොහුට ඒ සඳහා ඉතිරිව ඇත්තේ එක් කොටුවක් පමණි: ඒ කොටුව විවෘතයි."
        Rule.DEAD_END -> "විවෘත වුවහොත්, ${p.at(from[0])} හි කොටුව මළ කෙළවරක් වනු ඇත, එහි රාක්ෂයෙකු නැත: එබැවින් එය බිත්තියකි."
        Rule.PASSAGE -> "රාක්ෂයෙකු පමණක් මළ කෙළවරක සිටී: ${p.at(from[0])} හි කොටුවට පිටවීමේ මාර්ග දෙකක් අවශ්‍යයි, ඉතිරිව ඇත්තේ දෙකක් පමණි. දෙකම විවෘතයි."
        Rule.BLOCK -> "කොරිඩෝ පළලින් එක් කොටුවකි: විවෘත 2×2 කොටසක් නොතිබිය යුතුය. මෙහි කොටු තුනක් විවෘතයි, එබැවින් සිව්වැන්න බිත්තියකි."
        Rule.ROOM -> "මෙම පෙට්ටියේ 3×3 නිධි කාමරය කොතැනට ගැළපුණත්, එය ආවරණය කරන්නේ එකම කොටු ය: ඒවා විවෘතයි."
        Rule.EXIT ->
            if (open.isEmpty()) "නිධි කාමරයකට ඇත්තේ එක් පිටවීමක් පමණි, මෙයට එය ඇත: එය වටා ඇති අනෙක් සියලු කොටු බිත්ති."
            else "නිධි කාමරයකට ඇත්තේ එක් පිටවීමක් පමණි, මෙයට ඒ සඳහා ඉතිරිව ඇත්තේ එක් කොටුවක් පමණි: ඒ කොටුව විවෘතයි."
        Rule.CUT -> "සියලුම විවෘත කොටු සම්බන්ධයි. බිත්ති නිසා සමහර කොටු සිරගෙයේ ඉතිරි කොටසින් වෙන් වේ: එබැවින් ඒවාද බිත්ති."
        Rule.TRY -> {
            val (tried, proved) = if (assumes) "බිත්තියක්" to "විවෘත" else "විවෘත" to "බිත්තියක්"
            val end = when (breaks) {
                Rule.COUNT -> "පේළියක් හෝ තීරුවක බිත්ති ගණන වැරදී යයි"
                Rule.MONSTER -> "රාක්ෂයෙකුට පිටවීමට මාර්ගයක් නැති වේ, නැතහොත් දෙකක් වේ"
                Rule.BLOCK -> "විවෘත 2×2 කොටසක් ඇති වේ"
                Rule.ROOM, Rule.EXIT -> "නිධි කාමරයක් තවදුරටත් නොගැළපේ, නැතහොත් එකම පිටවීමක් නැත"
                Rule.CUT -> "සිරගෙයේ කොටසක් ඉතිරි කොටසින් වෙන් වේ"
                else -> "රාක්ෂයෙකු නැති තැනක මළ කෙළවරක් ඇති වේ"
            }
            "${p.at(from[0])} හි කොටුව $tried යැයි සිතන්න: එයින් සිදුවන දේ අනුගමනය කරන්න, එවිට $end. එබැවින් එය $proved."
        }
        Rule.EITHER -> "${p.at(from[0])} හි කොටුව දෙයාකාරයෙන්ම උත්සාහ කරන්න: බිත්තියක් හෝ විවෘත, සමහර කොටු දෙයාකාරයෙන්ම එකම ලෙස පැමිණේ. ඒවා නිසැකයි."
    }
}
