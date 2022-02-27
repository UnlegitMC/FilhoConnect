package today.getfdp.connect.translate.bedrock.play

import com.nukkitx.math.vector.Vector3f
import com.nukkitx.protocol.bedrock.packet.*
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockPlayStatusPacketTranslator : TranslatorBase<PlayStatusPacket> {

    override val intendedClass: Class<PlayStatusPacket>
        get() = PlayStatusPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: PlayStatusPacket) {
        when(packet.status) {
            PlayStatusPacket.Status.PLAYER_SPAWN -> {
                val interactPacket = InteractPacket()
                interactPacket.action = InteractPacket.Action.MOUSEOVER
                interactPacket.runtimeEntityId = 0
                interactPacket.mousePosition = Vector3f.from(0f, 0f, 0f)
                provider.bedrockPacketOut(interactPacket)

                val emoteListPacket = EmoteListPacket()
                emoteListPacket.runtimeEntityId = provider.client.thePlayer.runtimeId.toLong()
                provider.bedrockPacketOut(emoteListPacket)

                val setLocalPlayerAsInitializedPacket = SetLocalPlayerAsInitializedPacket()
                setLocalPlayerAsInitializedPacket.runtimeEntityId = provider.client.thePlayer.runtimeId.toLong()
                provider.bedrockPacketOut(setLocalPlayerAsInitializedPacket)
            }
        }
    }
}