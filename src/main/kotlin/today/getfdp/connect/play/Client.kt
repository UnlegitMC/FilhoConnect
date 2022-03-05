package today.getfdp.connect.play

import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.MinecraftConstants
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundDisconnectPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.ClientboundSetSubtitleTextPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.ClientboundSetTitleTextPacket
import com.github.steveice10.packetlib.Session
import com.github.steveice10.packetlib.packet.Packet
import com.nukkitx.protocol.bedrock.BedrockPacket
import net.kyori.adventure.text.Component
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.network.provider.PlayProvider
import today.getfdp.connect.play.entity.EntityThePlayer
import today.getfdp.connect.utils.protocol.BedrockLoginHelper
import java.util.*

class Client(val session: Session) {

    val profile: GameProfile
        get() = session.getFlag(MinecraftConstants.PROFILE_KEY) as GameProfile
    val name: String
        get() = profile.name
    val uuid: UUID
        get() = profile.id

    var commandsEnabled = true

    // objects to store in-game data
    val thePlayer = EntityThePlayer(this)
    val theWorld = TheWorld(this)

    // fabric mods support to enhance the experience
    val modManager = ModManager(this)

    fun fixUUID(uuid: UUID): UUID {
        return if(uuid == this.uuid) {
            loginHelper.identity
        } else {
            uuid
        }
    }

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

    fun send(packet: Packet) {
        session.send(packet)
    }

    fun send(packet: BedrockPacket) {
        (provider as BedrockProxyProvider).bedrockPacketOut(packet)
    }
}