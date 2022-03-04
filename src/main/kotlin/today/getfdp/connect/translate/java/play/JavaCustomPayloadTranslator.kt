package today.getfdp.connect.translate.java.play

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundCustomPayloadPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class JavaCustomPayloadTranslator : TranslatorBase<ServerboundCustomPayloadPacket> {

    override val intendedClass = ServerboundCustomPayloadPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ServerboundCustomPayloadPacket) {
        if(packet.channel == "minecraft:register") {
            provider.client.modManager.handleRegister(packet.data.toString(Charsets.UTF_8).split("\u0000"))
        }
        provider.client.modManager.handlePayload(packet.channel, packet.data)
    }
}