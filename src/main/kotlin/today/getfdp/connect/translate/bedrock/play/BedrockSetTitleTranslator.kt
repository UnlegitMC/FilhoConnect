package today.getfdp.connect.translate.bedrock.play

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.ClientboundClearTitlesPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.ClientboundSetActionBarTextPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.ClientboundSetSubtitleTextPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.ClientboundSetTitleTextPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.ClientboundSetTitlesAnimationPacket
import com.nukkitx.protocol.bedrock.packet.SetTitlePacket
import net.kyori.adventure.text.Component
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockSetTitleTranslator : TranslatorBase<SetTitlePacket> {

    override val intendedClass: Class<SetTitlePacket>
        get() = SetTitlePacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: SetTitlePacket) {
        provider.packetOut(when(packet.type) {
            SetTitlePacket.Type.TITLE, SetTitlePacket.Type.TITLE_JSON -> ClientboundSetTitleTextPacket(Component.text(packet.text))
            SetTitlePacket.Type.SUBTITLE, SetTitlePacket.Type.SUBTITLE_JSON -> ClientboundSetSubtitleTextPacket(Component.text(packet.text))
            SetTitlePacket.Type.ACTIONBAR, SetTitlePacket.Type.ACTIONBAR_JSON -> ClientboundSetActionBarTextPacket(Component.text(packet.text))
            SetTitlePacket.Type.TIMES -> ClientboundSetTitlesAnimationPacket(packet.fadeInTime, packet.stayTime, packet.fadeOutTime)
            SetTitlePacket.Type.CLEAR -> ClientboundClearTitlesPacket(false)
            else -> return
        })
    }
}