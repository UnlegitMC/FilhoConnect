package today.getfdp.connect.translate.bedrock.entity

import com.nukkitx.protocol.bedrock.packet.MoveEntityAbsolutePacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockMoveEntityAbsuloteTranslator : TranslatorBase<MoveEntityAbsolutePacket> {

    override val intendedClass: Class<MoveEntityAbsolutePacket>
        get() = MoveEntityAbsolutePacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: MoveEntityAbsolutePacket) {
        val entity = provider.client.theWorld.entities[packet.runtimeEntityId.toInt()] ?: return

        entity.updatePosition(packet.position)
        entity.updateRotation(packet.rotation)
        entity.onGround = packet.isOnGround

        entity.move()
    }
}