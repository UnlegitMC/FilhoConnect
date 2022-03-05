package today.getfdp.connect.translate.bedrock.entity.player

import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase
import today.getfdp.connect.utils.other.logWarn

class BedrockMovePlayerTranslator : TranslatorBase<MovePlayerPacket> {

    override val intendedClass: Class<MovePlayerPacket>
        get() = MovePlayerPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: MovePlayerPacket) {
        val entity = provider.client.theWorld.entities[packet.runtimeEntityId.toInt()]
        if(entity == null) {
            logWarn("Entity with runtimeId ${packet.runtimeEntityId} not found")
            return
        }

        entity.updatePosition(packet.position)
        entity.updateRotation(packet.rotation)
        entity.onGround = packet.isOnGround

        if(entity.runtimeId == provider.client.thePlayer.runtimeId) {
            provider.client.thePlayer.teleport()
        } else {
            entity.move()
        }
    }
}