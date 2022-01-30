package today.getfdp.connect.play

import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.MinecraftConstants
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundDisconnectPacket
import com.github.steveice10.packetlib.Session
import net.kyori.adventure.text.Component
import today.getfdp.connect.network.provider.PlayProvider

class Client(val session: Session) {

    val profile: GameProfile
        get() = session.getFlag(MinecraftConstants.PROFILE_KEY) as GameProfile
    val name: String
        get() = profile.name

    val flags = mutableMapOf<String, Any>()
    var isLogin = false

    var provider: PlayProvider? = null
        set(value) {
            field?.remove()
            field = value
            value?.apply(this)
        }

    fun disconnect(reason: String) {
        session.send(ClientboundDisconnectPacket(Component.text(reason)))
        session.disconnect(reason)
    }

    companion object {
        val FLAG_ACCESS_TOKEN = "accessToken"
    }
}