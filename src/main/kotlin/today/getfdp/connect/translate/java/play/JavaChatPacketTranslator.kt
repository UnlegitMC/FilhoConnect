package today.getfdp.connect.translate.java.play

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket
import com.nukkitx.protocol.bedrock.packet.TextPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class JavaChatPacketTranslator : TranslatorBase<ServerboundChatPacket> {

    override val intendedClass: Class<ServerboundChatPacket>
        get() = ServerboundChatPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ServerboundChatPacket) {
        val textPacket = TextPacket()
        textPacket.message = packet.message
        textPacket.type = TextPacket.Type.CHAT
        textPacket.isNeedsTranslation = false
        textPacket.sourceName = provider.client.loginHelper.displayName
        textPacket.xuid = provider.client.loginHelper.xuid
        provider.bedrockPacketOut(textPacket)
    }
}