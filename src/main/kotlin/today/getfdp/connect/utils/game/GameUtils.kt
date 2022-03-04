package today.getfdp.connect.utils.game

import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode
import com.github.steveice10.mc.protocol.data.game.setting.Difficulty
import com.nukkitx.protocol.bedrock.data.GameType
import net.kyori.adventure.text.format.NamedTextColor
import org.jline.utils.DiffHelper.Diff
import today.getfdp.connect.network.data.ScoreboardSortOrder
import today.getfdp.connect.utils.other.logWarn

object GameUtils {

    private val difficulties = Difficulty.values() // this will improve performance

    fun convertToGameMode(gameType: GameType): GameMode {
        return when(gameType) {
            GameType.SURVIVAL -> GameMode.SURVIVAL
            GameType.CREATIVE -> GameMode.CREATIVE
            GameType.ADVENTURE -> GameMode.ADVENTURE
            GameType.SURVIVAL_VIEWER, GameType.CREATIVE_VIEWER -> GameMode.SPECTATOR
            else -> GameMode.UNKNOWN // these game types are not exists in Java Edition
        }
    }

    fun getDifficulty(code: Int): Difficulty {
        return difficulties.getOrNull(code) ?: Difficulty.NORMAL
    }

    fun getScoreboardSortOrder(order: Int): ScoreboardSortOrder {
        val sortOrder = ScoreboardSortOrder.values.getOrNull(order)
        if (sortOrder == null) {
            logWarn("Unknown scoreboard sort order: $order")
            return ScoreboardSortOrder.DESCENDING
        }
        return sortOrder
    }

    fun getColorByCode(code: String): NamedTextColor {
        when(code) {
            "0" -> return NamedTextColor.BLACK
            "1" -> return NamedTextColor.DARK_BLUE
            "2" -> return NamedTextColor.DARK_GREEN
            "3" -> return NamedTextColor.DARK_AQUA
            "4" -> return NamedTextColor.DARK_RED
            "5" -> return NamedTextColor.DARK_PURPLE
            "6" -> return NamedTextColor.GOLD
            "7" -> return NamedTextColor.GRAY
            "8" -> return NamedTextColor.DARK_GRAY
            "9" -> return NamedTextColor.BLUE
            "a" -> return NamedTextColor.GREEN
            "b" -> return NamedTextColor.AQUA
            "c" -> return NamedTextColor.RED
            "d" -> return NamedTextColor.LIGHT_PURPLE
            "e" -> return NamedTextColor.YELLOW
            else -> return NamedTextColor.WHITE
        }
    }
}