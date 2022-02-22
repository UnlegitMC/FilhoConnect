package today.getfdp.connect.translate.bedrock.connect

import com.nukkitx.protocol.bedrock.packet.DisconnectPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockDisconnectPacketTranslator : TranslatorBase<DisconnectPacket> {

    override val intendedClass: Class<DisconnectPacket>
        get() = DisconnectPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: DisconnectPacket) {
        provider.client.disconnect("${if(packet.isMessageSkipped) { "(Skipped)" } else { "" }}${packet.kickMessage}")
    }
}