package today.getfdp.connect.translate.bedrock.entity

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket
import com.nukkitx.protocol.bedrock.packet.AddEntityPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockAddEntityTranslator : TranslatorBase<AddEntityPacket> {

    override val intendedClass: Class<AddEntityPacket>
        get() = AddEntityPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: AddEntityPacket) {
    }
}