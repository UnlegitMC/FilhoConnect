package today.getfdp.connect.network

import com.github.steveice10.mc.auth.data.GameProfile
import java.util.*

data class Motd(val message: String, val nowPlayers: Int, val maxPlayers: Int, val extraMessage: String = "", val icon: ByteArray? = null) {

    val gameProfileArr: Array<GameProfile>
        get() = if(extraMessage.isEmpty()) { emptyArray() } else {
            extraMessage.split("\n").map { GameProfile(UUID.randomUUID(), it) }.toTypedArray()
        }
}