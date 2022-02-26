package today.getfdp.connect.translate.bedrock.connect

import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket
import com.nukkitx.protocol.bedrock.packet.ResourcePackStackPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockResourcePackStackPacketTranslator : TranslatorBase<ResourcePackStackPacket> {

    override val intendedClass: Class<ResourcePackStackPacket>
        get() = ResourcePackStackPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ResourcePackStackPacket) {
        val respPacket = ResourcePackClientResponsePacket()
        respPacket.status = ResourcePackClientResponsePacket.Status.COMPLETED
        provider.bedrockPacketOut(respPacket)
    }
}