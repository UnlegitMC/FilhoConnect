package today.getfdp.connect.translate.bedrock.world

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChangeDifficultyPacket
import com.nukkitx.protocol.bedrock.packet.SetDifficultyPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase
import today.getfdp.connect.utils.game.GameUtils

class BedrockSetDifficultyTranslator : TranslatorBase<SetDifficultyPacket> {

    override val intendedClass: Class<SetDifficultyPacket>
        get() = SetDifficultyPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: SetDifficultyPacket) {
        provider.packetOut(ClientboundChangeDifficultyPacket(GameUtils.getDifficulty(packet.difficulty), false))
    }
}