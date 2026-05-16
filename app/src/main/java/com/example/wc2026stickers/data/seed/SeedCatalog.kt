package com.wc2026stickers.app.data.seed

data class TeamSeedEntry(
    val code: String,
    val name: String,
    val flag: String,
    val confederation: String,
    val sortOrder: Int
)

object SeedCatalog {
    const val specialTeamCode = "FWC"
    private const val stickersPerSection = 20

    val teams: List<TeamSeedEntry> = listOf(
        TeamSeedEntry(specialTeamCode, "Special Stickers", "\u2B50", "SPECIAL", 0),

        TeamSeedEntry("MEX", "Mexico", "\uD83C\uDDF2\uD83C\uDDFD", "CONCACAF", 10),
        TeamSeedEntry("USA", "United States", "\uD83C\uDDFA\uD83C\uDDF8", "CONCACAF", 11),
        TeamSeedEntry("CAN", "Canada", "\uD83C\uDDE8\uD83C\uDDE6", "CONCACAF", 12),
        TeamSeedEntry("PAN", "Panama", "\uD83C\uDDF5\uD83C\uDDE6", "CONCACAF", 13),
        TeamSeedEntry("CUW", "Curacao", "\uD83C\uDDE8\uD83C\uDDFC", "CONCACAF", 14),
        TeamSeedEntry("HAI", "Haiti", "\uD83C\uDDED\uD83C\uDDF9", "CONCACAF", 15),

        TeamSeedEntry("ENG", "England", "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F", "UEFA", 20),
        TeamSeedEntry("FRA", "France", "\uD83C\uDDEB\uD83C\uDDF7", "UEFA", 21),
        TeamSeedEntry("ESP", "Spain", "\uD83C\uDDEA\uD83C\uDDF8", "UEFA", 22),
        TeamSeedEntry("GER", "Germany", "\uD83C\uDDE9\uD83C\uDDEA", "UEFA", 23),
        TeamSeedEntry("NED", "Netherlands", "\uD83C\uDDF3\uD83C\uDDF1", "UEFA", 24),
        TeamSeedEntry("POR", "Portugal", "\uD83C\uDDF5\uD83C\uDDF9", "UEFA", 25),
        TeamSeedEntry("BEL", "Belgium", "\uD83C\uDDE7\uD83C\uDDEA", "UEFA", 26),
        TeamSeedEntry("CRO", "Croatia", "\uD83C\uDDED\uD83C\uDDF7", "UEFA", 27),
        TeamSeedEntry("SUI", "Switzerland", "\uD83C\uDDE8\uD83C\uDDED", "UEFA", 28),
        TeamSeedEntry("AUT", "Austria", "\uD83C\uDDE6\uD83C\uDDF9", "UEFA", 29),
        TeamSeedEntry("NOR", "Norway", "\uD83C\uDDF3\uD83C\uDDF4", "UEFA", 30),
        TeamSeedEntry("SCO", "Scotland", "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC73\uDB40\uDC63\uDB40\uDC74\uDB40\uDC7F", "UEFA", 31),
        TeamSeedEntry("SWE", "Sweden", "\uD83C\uDDF8\uD83C\uDDEA", "UEFA", 32),
        TeamSeedEntry("TUR", "Turkiye", "\uD83C\uDDF9\uD83C\uDDF7", "UEFA", 33),
        TeamSeedEntry("BIH", "Bosnia & Herzegovina", "\uD83C\uDDE7\uD83C\uDDE6", "UEFA", 34),
        TeamSeedEntry("CZE", "Czechia", "\uD83C\uDDE8\uD83C\uDDFF", "UEFA", 35),

        TeamSeedEntry("ARG", "Argentina", "\uD83C\uDDE6\uD83C\uDDF7", "CONMEBOL", 50),
        TeamSeedEntry("BRA", "Brazil", "\uD83C\uDDE7\uD83C\uDDF7", "CONMEBOL", 51),
        TeamSeedEntry("COL", "Colombia", "\uD83C\uDDE8\uD83C\uDDF4", "CONMEBOL", 52),
        TeamSeedEntry("ECU", "Ecuador", "\uD83C\uDDEA\uD83C\uDDE8", "CONMEBOL", 53),
        TeamSeedEntry("PAR", "Paraguay", "\uD83C\uDDF5\uD83C\uDDFE", "CONMEBOL", 54),
        TeamSeedEntry("URU", "Uruguay", "\uD83C\uDDFA\uD83C\uDDFE", "CONMEBOL", 55),

        TeamSeedEntry("MAR", "Morocco", "\uD83C\uDDF2\uD83C\uDDE6", "CAF", 60),
        TeamSeedEntry("SEN", "Senegal", "\uD83C\uDDF8\uD83C\uDDF3", "CAF", 61),
        TeamSeedEntry("EGY", "Egypt", "\uD83C\uDDEA\uD83C\uDDEC", "CAF", 62),
        TeamSeedEntry("GHA", "Ghana", "\uD83C\uDDEC\uD83C\uDDED", "CAF", 63),
        TeamSeedEntry("CIV", "Cote d'Ivoire", "\uD83C\uDDE8\uD83C\uDDEE", "CAF", 64),
        TeamSeedEntry("ALG", "Algeria", "\uD83C\uDDE9\uD83C\uDDFF", "CAF", 65),
        TeamSeedEntry("TUN", "Tunisia", "\uD83C\uDDF9\uD83C\uDDF3", "CAF", 66),
        TeamSeedEntry("RSA", "South Africa", "\uD83C\uDDFF\uD83C\uDDE6", "CAF", 67),
        TeamSeedEntry("CPV", "Cape Verde", "\uD83C\uDDE8\uD83C\uDDFB", "CAF", 68),
        TeamSeedEntry("COD", "DR Congo", "\uD83C\uDDE8\uD83C\uDDE9", "CAF", 69),

        TeamSeedEntry("JPN", "Japan", "\uD83C\uDDEF\uD83C\uDDF5", "AFC", 80),
        TeamSeedEntry("KOR", "South Korea", "\uD83C\uDDF0\uD83C\uDDF7", "AFC", 81),
        TeamSeedEntry("IRN", "Iran", "\uD83C\uDDEE\uD83C\uDDF7", "AFC", 82),
        TeamSeedEntry("AUS", "Australia", "\uD83C\uDDE6\uD83C\uDDFA", "AFC", 83),
        TeamSeedEntry("KSA", "Saudi Arabia", "\uD83C\uDDF8\uD83C\uDDE6", "AFC", 84),
        TeamSeedEntry("QAT", "Qatar", "\uD83C\uDDF6\uD83C\uDDE6", "AFC", 85),
        TeamSeedEntry("UZB", "Uzbekistan", "\uD83C\uDDFA\uD83C\uDDFF", "AFC", 86),
        TeamSeedEntry("IRQ", "Iraq", "\uD83C\uDDEE\uD83C\uDDF6", "AFC", 87),
        TeamSeedEntry("JOR", "Jordan", "\uD83C\uDDEF\uD83C\uDDF4", "AFC", 88),

        TeamSeedEntry("NZL", "New Zealand", "\uD83C\uDDF3\uD83C\uDDFF", "OFC", 90)
    )

    val expectedTeamCount: Int = teams.size
    val expectedStickerCount: Int = teams.size * stickersPerSection
}
