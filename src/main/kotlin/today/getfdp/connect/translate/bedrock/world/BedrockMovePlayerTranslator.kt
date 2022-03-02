package today.getfdp.connect.translate.bedrock.world

import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockMovePlayerTranslator : TranslatorBase<MovePlayerPacket> {

    override val intendedClass: Class<MovePlayerPacket>
        get() = MovePlayerPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: MovePlayerPacket) {
        if(packet.runtimeEntityId.toInt() == provider.client.thePlayer.runtimeId) {
            provider.client.thePlayer.updatePosition(packet.position)
            provider.client.thePlayer.updateRotation(packet.rotation)
            provider.client.thePlayer.teleport()
        }
    }
}