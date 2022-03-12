package today.getfdp.connect.translate.bedrock.entity

import com.nukkitx.protocol.bedrock.packet.RemoveEntityPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockRemoveEntityTranslator : TranslatorBase<RemoveEntityPacket> {

    override val intendedClass: Class<RemoveEntityPacket>
        get() = RemoveEntityPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: RemoveEntityPacket) {
        val entity = provider.client.theWorld.entities[packet.uniqueEntityId.toInt()] ?: return

        entity.despawn()
    }
}