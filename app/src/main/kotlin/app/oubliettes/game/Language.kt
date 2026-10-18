package app.oubliettes.game

/**
 * Whether the words the game itself writes (hints and lessons) are in Sinhala. The app sets it from
 * its language setting; the game logic only reads it. English until told otherwise.
 */
object Language {
    @Volatile
    var sinhala = false
}
