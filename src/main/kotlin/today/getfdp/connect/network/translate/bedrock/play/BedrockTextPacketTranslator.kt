package today.getfdp.connect.network.translate.bedrock.play

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket
import com.nukkitx.protocol.bedrock.packet.TextPacket
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.network.translate.TranslatorBase
import today.getfdp.connect.utils.game.GameUtils

class BedrockTextPacketTranslator : TranslatorBase<TextPacket> {

    override val intendedClass: Class<TextPacket>
        get() = TextPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: TextPacket) {
        val component = when (packet.type) {
            TextPacket.Type.CHAT -> Component.text("<${packet.sourceName}> ${packet.message}").hoverEvent(Component.text("xuid: ${packet.xuid}"))
            TextPacket.Type.ANNOUNCEMENT -> Component.text("[!] ${packet.message}")
            TextPacket.Type.WHISPER -> Component.text("[${packet.sourceName} -> Me] ${packet.message}")
            else -> {
                if(packet.isNeedsTranslation) {
                    processTranslatable(packet.message, packet.parameters)
                } else {
                    Component.text(packet.message)
                }
            }
        }
        provider.packetOut(ClientboundChatPacket(component))
    }

    companion object {
        private val translatableRegex = Regex("%[a-z,.]{3,}")

        fun processTranslatable(message: String, parameters: List<String>): Component {
            val componentParameters = parameters.map { Component.text(it) }
            if(!message.contains("%")) {
                return Component.translatable(message, componentParameters)
            }
            val matches = translatableRegex.findAll(message).toList()
            val raws = message.split(translatableRegex)
            var color = processColor(raws.first())
            var component = Component.text(raws.first())
            matches.forEach {
                component = component.append(Component.translatable(it.value.substring(1), componentParameters).color(color))
                if(raws.size > matches.indexOf(it) + 1) {
                    val raw = raws[matches.indexOf(it) + 1]
                    component = component.append(Component.text(raw).color(color))
                    color = processColor(raw)
                }
            }
            return component
        }

        private fun processColor(raw: String): NamedTextColor {
            val spice = raw.split("§")
            return if(spice.size > 1) {
                GameUtils.getColorByCode(spice[1].substring(0, 1))
            } else {
                NamedTextColor.WHITE
            }
        }
    }
}