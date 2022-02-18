package today.getfdp.connect.utils.game

import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode
import com.nukkitx.protocol.bedrock.data.GameType

object GameUtils {

    fun convertToGameMode(gameType: GameType): GameMode {
        return when(gameType) {
            GameType.SURVIVAL -> GameMode.SURVIVAL
            GameType.CREATIVE -> GameMode.CREATIVE
            GameType.ADVENTURE -> GameMode.ADVENTURE
            GameType.SURVIVAL_VIEWER, GameType.CREATIVE_VIEWER -> GameMode.SPECTATOR
            else -> GameMode.UNKNOWN // these game types are not exists in Java Edition
        }
    }
}