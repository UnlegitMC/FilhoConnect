package today.getfdp.connect.translate.bedrock.entity.player

import com.nukkitx.protocol.bedrock.packet.AddPlayerPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.play.entity.EntityPlayer
import today.getfdp.connect.translate.TranslatorBase
import today.getfdp.connect.utils.game.LengthUtils

class BedrockAddPlayerTranslator : TranslatorBase<AddPlayerPacket> {

    override val intendedClass: Class<AddPlayerPacket>
        get() = AddPlayerPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: AddPlayerPacket) {
        val entity = EntityPlayer(provider.client)

        entity.runtimeId = packet.runtimeEntityId.toInt()
        entity.updatePositionAbsulute(packet.position)
        entity.updateRotation(packet.rotation)
        entity.name = LengthUtils.forName(packet.username)
        entity.uuid = packet.uuid

        provider.client.theWorld.spawn(entity)
    }
}