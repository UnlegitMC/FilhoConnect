package today.getfdp.connect.network.translate.bedrock.connect

import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket
import com.nukkitx.protocol.bedrock.packet.ResourcePacksInfoPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.network.translate.TranslatorBase

class BedrockResourcePacksInfoPacketTranslator : TranslatorBase<ResourcePacksInfoPacket> {

    override val intendedClass: Class<ResourcePacksInfoPacket>
        get() = ResourcePacksInfoPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ResourcePacksInfoPacket) {
        val respPacket = ResourcePackClientResponsePacket()
        respPacket.status = ResourcePackClientResponsePacket.Status.HAVE_ALL_PACKS
        provider.bedrockPacketOut(respPacket)
    }
}