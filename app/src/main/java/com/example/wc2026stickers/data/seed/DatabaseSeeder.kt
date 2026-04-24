package com.example.wc2026stickers.data.seed

import android.content.Context
import android.content.SharedPreferences
import androidx.room.withTransaction
import com.example.wc2026stickers.data.db.AppDatabase
import com.example.wc2026stickers.data.db.dao.StickerDao
import com.example.wc2026stickers.data.db.dao.TeamDao
import com.example.wc2026stickers.data.db.entities.Sticker
import com.example.wc2026stickers.data.db.entities.StickerType
import com.example.wc2026stickers.data.db.entities.Team
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val teamDao: TeamDao,
    private val stickerDao: StickerDao,
    private val db: AppDatabase,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("wc2026_prefs", Context.MODE_PRIVATE)

    suspend fun seedIfNeeded() {
        if (prefs.getBoolean("seeded", false)) return
        db.withTransaction {
            seedTeams()
            seedStickers()
        }
        prefs.edit().putBoolean("seeded", true).apply()
    }

    private suspend fun seedTeams() {
        allTeams().forEach { (code, name, flag, conf, sort) ->
            teamDao.insertTeam(Team(code, name, flag, conf, sort))
        }
    }

    private suspend fun seedStickers() {
        // --- FWC special section (19 stickers) ---
        val fwcLabels = listOf(
            "Official Emblem 1",
            "Official Emblem 2",
            "Official Mascots",
            "Official Slogan",
            "Official Ball",
            "Host Country: Canada",
            "Host Country: Mexico",
            "Host Country: USA",
            "History: Italy 1934",
            "History: Uruguay 1950",
            "History: West Germany 1954",
            "History: Brazil 1962",
            "History: West Germany 1974",
            "History: Argentina 1986",
            "History: Brazil 1994",
            "History: Brazil 2002",
            "History: Italy 2006",
            "History: Germany 2014",
            "History: Argentina 2022"
        )
        fwcLabels.forEachIndexed { i, label ->
            stickerDao.insertSticker(
                Sticker(
                    id = "FWC-${i + 1}",
                    teamCode = "FWC",
                    number = i + 1,
                    label = label,
                    stickerType = StickerType.SPECIAL,
                    isShiny = true
                )
            )
        }

        // --- 48 team sections x 20 stickers = 960 stickers ---
        // Total: 19 FWC + 960 team = 979 stickers
        // UserSticker rows are NOT seeded — LEFT JOIN + COALESCE treats absent rows as quantity=0
        allTeams().forEach { (code, _, _, _, _) ->
            if (code == "FWC") return@forEach
            for (num in 1..20) {
                val label = when (num) {
                    1 -> "Badge"
                    2 -> "Team Photo"
                    else -> "Player ${num - 2}"
                }
                val type = when (num) {
                    1 -> StickerType.BADGE
                    2 -> StickerType.TEAM_PHOTO
                    else -> StickerType.PLAYER
                }
                stickerDao.insertSticker(
                    Sticker(
                        id = "$code-$num",
                        teamCode = code,
                        number = num,
                        label = label,
                        stickerType = type,
                        isShiny = num == 1
                    )
                )
            }
        }
    }

    /**
     * All 48 confirmed FIFA World Cup 2026 qualified teams (verified April 2026) + FWC special section.
     *   CONCACAF: 6 (3 hosts + Panama, Curacao, Haiti)
     *   UEFA:    16
     *   CONMEBOL: 6
     *   CAF:     10
     *   AFC:      9
     *   OFC:      1
     *   Total:   48 x 20 + 19 FWC = 979 stickers
     */
    private fun allTeams(): List<TeamEntry> = listOf(
        // Special intro section
        TeamEntry("FWC", "Special Stickers", "\u2B50", "SPECIAL", 0),

        // CONCACAF (6) — hosts first
        TeamEntry("MEX", "Mexico", "\uD83C\uDDF2\uD83C\uDDFD", "CONCACAF", 10),
        TeamEntry("USA", "United States", "\uD83C\uDDFA\uD83C\uDDF8", "CONCACAF", 11),
        TeamEntry("CAN", "Canada", "\uD83C\uDDE8\uD83C\uDDE6", "CONCACAF", 12),
        TeamEntry("PAN", "Panama", "\uD83C\uDDF5\uD83C\uDDE6", "CONCACAF", 13),
        TeamEntry("CUW", "Curacao", "\uD83C\uDDE8\uD83C\uDDFC", "CONCACAF", 14),
        TeamEntry("HAI", "Haiti", "\uD83C\uDDED\uD83C\uDDF9", "CONCACAF", 15),

        // UEFA (16)
        TeamEntry("ENG", "England", "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F", "UEFA", 20),
        TeamEntry("FRA", "France", "\uD83C\uDDEB\uD83C\uDDF7", "UEFA", 21),
        TeamEntry("ESP", "Spain", "\uD83C\uDDEA\uD83C\uDDF8", "UEFA", 22),
        TeamEntry("GER", "Germany", "\uD83C\uDDE9\uD83C\uDDEA", "UEFA", 23),
        TeamEntry("NED", "Netherlands", "\uD83C\uDDF3\uD83C\uDDF1", "UEFA", 24),
        TeamEntry("POR", "Portugal", "\uD83C\uDDF5\uD83C\uDDF9", "UEFA", 25),
        TeamEntry("BEL", "Belgium", "\uD83C\uDDE7\uD83C\uDDEA", "UEFA", 26),
        TeamEntry("CRO", "Croatia", "\uD83C\uDDED\uD83C\uDDF7", "UEFA", 27),
        TeamEntry("SUI", "Switzerland", "\uD83C\uDDE8\uD83C\uDDED", "UEFA", 28),
        TeamEntry("AUT", "Austria", "\uD83C\uDDE6\uD83C\uDDF9", "UEFA", 29),
        TeamEntry("NOR", "Norway", "\uD83C\uDDF3\uD83C\uDDF4", "UEFA", 30),
        TeamEntry("SCO", "Scotland", "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC73\uDB40\uDC63\uDB40\uDC74\uDB40\uDC7F", "UEFA", 31),
        TeamEntry("SWE", "Sweden", "\uD83C\uDDF8\uD83C\uDDEA", "UEFA", 32),
        TeamEntry("TUR", "Turkiye", "\uD83C\uDDF9\uD83C\uDDF7", "UEFA", 33),
        TeamEntry("BIH", "Bosnia & Herzegovina", "\uD83C\uDDE7\uD83C\uDDE6", "UEFA", 34),
        TeamEntry("CZE", "Czechia", "\uD83C\uDDE8\uD83C\uDDFF", "UEFA", 35),

        // CONMEBOL (6)
        TeamEntry("ARG", "Argentina", "\uD83C\uDDE6\uD83C\uDDF7", "CONMEBOL", 50),
        TeamEntry("BRA", "Brazil", "\uD83C\uDDE7\uD83C\uDDF7", "CONMEBOL", 51),
        TeamEntry("COL", "Colombia", "\uD83C\uDDE8\uD83C\uDDF4", "CONMEBOL", 52),
        TeamEntry("ECU", "Ecuador", "\uD83C\uDDEA\uD83C\uDDE8", "CONMEBOL", 53),
        TeamEntry("PAR", "Paraguay", "\uD83C\uDDF5\uD83C\uDDFE", "CONMEBOL", 54),
        TeamEntry("URU", "Uruguay", "\uD83C\uDDFA\uD83C\uDDFE", "CONMEBOL", 55),

        // CAF (10)
        TeamEntry("MAR", "Morocco", "\uD83C\uDDF2\uD83C\uDDE6", "CAF", 60),
        TeamEntry("SEN", "Senegal", "\uD83C\uDDF8\uD83C\uDDF3", "CAF", 61),
        TeamEntry("EGY", "Egypt", "\uD83C\uDDEA\uD83C\uDDEC", "CAF", 62),
        TeamEntry("GHA", "Ghana", "\uD83C\uDDEC\uD83C\uDDED", "CAF", 63),
        TeamEntry("CIV", "Cote d'Ivoire", "\uD83C\uDDE8\uD83C\uDDEE", "CAF", 64),
        TeamEntry("ALG", "Algeria", "\uD83C\uDDE9\uD83C\uDDFF", "CAF", 65),
        TeamEntry("TUN", "Tunisia", "\uD83C\uDDF9\uD83C\uDDF3", "CAF", 66),
        TeamEntry("RSA", "South Africa", "\uD83C\uDDFF\uD83C\uDDE6", "CAF", 67),
        TeamEntry("CPV", "Cape Verde", "\uD83C\uDDE8\uD83C\uDDFB", "CAF", 68),
        TeamEntry("COD", "DR Congo", "\uD83C\uDDE8\uD83C\uDDE9", "CAF", 69),

        // AFC (9)
        TeamEntry("JPN", "Japan", "\uD83C\uDDEF\uD83C\uDDF5", "AFC", 80),
        TeamEntry("KOR", "South Korea", "\uD83C\uDDF0\uD83C\uDDF7", "AFC", 81),
        TeamEntry("IRN", "Iran", "\uD83C\uDDEE\uD83C\uDDF7", "AFC", 82),
        TeamEntry("AUS", "Australia", "\uD83C\uDDE6\uD83C\uDDFA", "AFC", 83),
        TeamEntry("SAU", "Saudi Arabia", "\uD83C\uDDF8\uD83C\uDDE6", "AFC", 84),
        TeamEntry("QAT", "Qatar", "\uD83C\uDDF6\uD83C\uDDE6", "AFC", 85),
        TeamEntry("UZB", "Uzbekistan", "\uD83C\uDDFA\uD83C\uDDFF", "AFC", 86),
        TeamEntry("IRQ", "Iraq", "\uD83C\uDDEE\uD83C\uDDF6", "AFC", 87),
        TeamEntry("JOR", "Jordan", "\uD83C\uDDEF\uD83C\uDDF4", "AFC", 88),

        // OFC (1)
        TeamEntry("NZL", "New Zealand", "\uD83C\uDDF3\uD83C\uDDFF", "OFC", 90)
    )
    // 6 + 16 + 6 + 10 + 9 + 1 = 48 teams confirmed

    private data class TeamEntry(
        val code: String,
        val name: String,
        val flag: String,
        val confederation: String,
        val sortOrder: Int
    )
}
