package today.getfdp.connect.translate.bedrock.entity

import com.nukkitx.protocol.bedrock.packet.MoveEntityDeltaPacket
import com.nukkitx.protocol.bedrock.v291.serializer.MoveEntityDeltaSerializer_v291
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockMoveEntityDeltaTranslator : TranslatorBase<MoveEntityDeltaPacket> {

    override val intendedClass: Class<MoveEntityDeltaPacket>
        get() = MoveEntityDeltaPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: MoveEntityDeltaPacket) {
        val entity = provider.client.theWorld.entities[packet.runtimeEntityId.toInt()] ?: return

        println(packet)
        if(packet.flags.contains(MoveEntityDeltaPacket.Flag.HAS_X)) { // todo: check if this is correct
            entity.posX += packet.deltaX
        }
        if(packet.flags.contains(MoveEntityDeltaPacket.Flag.HAS_Y)) {
            entity.posY += packet.deltaY
        }
        if(packet.flags.contains(MoveEntityDeltaPacket.Flag.HAS_Z)) {
            entity.posZ += packet.deltaZ
        }
        if(packet.flags.contains(MoveEntityDeltaPacket.Flag.HAS_YAW)) {
            entity.rotationYaw = packet.yaw
        }
        if(packet.flags.contains(MoveEntityDeltaPacket.Flag.HAS_PITCH)) {
            entity.rotationPitch = packet.pitch
        }
        entity.onGround = packet.flags.contains(MoveEntityDeltaPacket.Flag.ON_GROUND)
    }
}