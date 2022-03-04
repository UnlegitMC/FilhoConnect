package today.getfdp.connect.translate.bedrock.world.scoreboard

import com.github.steveice10.mc.protocol.data.game.scoreboard.ObjectiveAction
import com.github.steveice10.mc.protocol.data.game.scoreboard.ScoreType
import com.github.steveice10.mc.protocol.data.game.scoreboard.ScoreboardPosition
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetDisplayObjectivePacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetObjectivePacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetScorePacket
import com.nukkitx.protocol.bedrock.packet.SetDisplayObjectivePacket
import net.kyori.adventure.text.Component
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase
import today.getfdp.connect.utils.game.GameUtils

class BedrockSetDisplayObjectiveTranslator : TranslatorBase<SetDisplayObjectivePacket> {

    override val intendedClass: Class<SetDisplayObjectivePacket>
        get() = SetDisplayObjectivePacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: SetDisplayObjectivePacket) {
        when(packet.displaySlot) {
            SLOT_SIDEBAR -> {
                provider.packetOut(ClientboundSetObjectivePacket(packet.objectiveId, ObjectiveAction.ADD, Component.text(packet.displayName), ScoreType.INTEGER))
                provider.packetOut(ClientboundSetDisplayObjectivePacket(ScoreboardPosition.SIDEBAR, packet.objectiveId))
                provider.client.theWorld.scoreboardSorts[packet.objectiveId] = GameUtils.getScoreboardSortOrder(packet.sortOrder)
            }
        }
    }

    companion object {
        const val SLOT_SIDEBAR = "sidebar"
        // todo: add more
    }
}