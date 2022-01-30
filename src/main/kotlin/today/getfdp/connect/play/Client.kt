package today.getfdp.connect.play

import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.MinecraftConstants
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundDisconnectPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.ClientboundSetSubtitleTextPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.ClientboundSetTitleTextPacket
import com.github.steveice10.packetlib.Session
import net.kyori.adventure.text.Component
import today.getfdp.connect.network.provider.PlayProvider
import today.getfdp.connect.network.utility.BedrockLoginHelper
import java.util.*

class Client(val session: Session) {

    val profile: GameProfile
        get() = session.getFlag(MinecraftConstants.PROFILE_KEY) as GameProfile
    val name: String
        get() = profile.name
    val uuid: UUID
        get() = profile.id

    val flags = mutableMapOf<String, Any>()
    var isLogin = false

    /**
     * this class stores the profile of the player in the bedrock server
     */
    val loginHelper = BedrockLoginHelper(this)

    var provider: PlayProvider? = null
        set(value) {
            if(field == value) return
            try {
                field?.remove()
                field = value
                value?.apply(this)
            } catch (t: Throwable) {
                t.printStackTrace()
                session.disconnect("Failed to apply provider")
            }
        }

    fun disconnect(reason: String) {
        provider = null
        session.send(ClientboundDisconnectPacket(Component.text(reason)))
    }

    fun title(title: String) {
        session.send(ClientboundSetTitleTextPacket(Component.text(title)))
    }

    fun subtitle(subtitle: String) {
        session.send(ClientboundSetSubtitleTextPacket(Component.text(subtitle)))
    }

    fun chat(message: String) {
        session.send(ClientboundChatPacket(Component.text(message)))
    }

    fun chat(message: Component) {
        session.send(ClientboundChatPacket(message))
    }

    companion object {
        val FLAG_ACCESS_TOKEN = "accessToken"
    }
}