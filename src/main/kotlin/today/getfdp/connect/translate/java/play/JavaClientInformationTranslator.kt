package today.getfdp.connect.translate.java.play

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundClientInformationPacket
import com.nukkitx.protocol.bedrock.packet.RequestChunkRadiusPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class JavaClientInformationTranslator : TranslatorBase<ServerboundClientInformationPacket> {

    override val intendedClass: Class<ServerboundClientInformationPacket>
        get() = ServerboundClientInformationPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ServerboundClientInformationPacket) {
        val requestChunkRadiusPacket = RequestChunkRadiusPacket() // we need to request chunks, or we will get no chunks on pmmp servers
        requestChunkRadiusPacket.radius = packet.renderDistance
        provider.bedrockPacketOut(requestChunkRadiusPacket)
    }
}