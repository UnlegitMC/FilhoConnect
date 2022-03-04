package today.getfdp.connect.translate.bedrock.world.scoreboard

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetScorePacket
import com.nukkitx.protocol.bedrock.packet.SetScorePacket
import today.getfdp.connect.network.data.ScoreboardSortOrder
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockSetScoreTranslator : TranslatorBase<SetScorePacket> {

    override val intendedClass: Class<SetScorePacket>
        get() = SetScorePacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: SetScorePacket) {
        packet.infos.forEach { info ->
            val order = provider.client.theWorld.scoreboardSorts[info.objectiveId] ?: ScoreboardSortOrder.DESCENDING
            provider.packetOut(when(packet.action) {
                SetScorePacket.Action.SET -> ClientboundSetScorePacket(info.name, info.objectiveId,
                    (if(order == ScoreboardSortOrder.DESCENDING) 1 else -1) * info.score) // java edition don't support ascending :(
                SetScorePacket.Action.REMOVE -> ClientboundSetScorePacket(info.name, info.objectiveId)
            })
        }
    }
}