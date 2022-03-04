package today.getfdp.connect.translate.bedrock.world.scoreboard

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetObjectivePacket
import com.nukkitx.protocol.bedrock.packet.RemoveObjectivePacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockRemoveObjectiveTranslator : TranslatorBase<RemoveObjectivePacket> {

    override val intendedClass: Class<RemoveObjectivePacket>
        get() = RemoveObjectivePacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: RemoveObjectivePacket) {
        provider.packetOut(ClientboundSetObjectivePacket(packet.objectiveId))
    }
}