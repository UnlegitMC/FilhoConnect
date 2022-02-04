package today.getfdp.connect.network.translate.bedrock.connect

import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket
import com.nukkitx.protocol.bedrock.packet.ResourcePackStackPacket
import today.getfdp.connect.FConnect
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.network.translate.TranslatorBase
import today.getfdp.connect.utils.other.logWarn

class BedrockResourcePackStackPacketTranslator : TranslatorBase<ResourcePackStackPacket> {

    override val intendedClass: Class<ResourcePackStackPacket>
        get() = ResourcePackStackPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ResourcePackStackPacket) {
        val respPacket = ResourcePackClientResponsePacket()
        respPacket.status = ResourcePackClientResponsePacket.Status.COMPLETED
        provider.bedrockPacketOut(respPacket)

        if(packet.gameVersion != FConnect.bedrockCodec.minecraftVersion) {
            logWarn("Server GameVersion not match! (${packet.gameVersion})")
        }
    }
}