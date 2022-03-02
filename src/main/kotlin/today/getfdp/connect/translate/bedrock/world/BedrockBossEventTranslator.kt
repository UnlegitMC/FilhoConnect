package today.getfdp.connect.translate.bedrock.world

import com.github.steveice10.mc.protocol.data.game.BossBarAction
import com.github.steveice10.mc.protocol.data.game.BossBarColor
import com.github.steveice10.mc.protocol.data.game.BossBarDivision
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundBossEventPacket
import com.nukkitx.protocol.bedrock.packet.BossEventPacket
import net.kyori.adventure.text.Component
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase
import java.util.UUID

class BedrockBossEventTranslator : TranslatorBase<BossEventPacket> {

    private val bossBarColors = BossBarColor.values() // this will improve performance

    override val intendedClass: Class<BossEventPacket>
        get() = BossEventPacket::class.java

    private fun colorFromCode(code: Int): BossBarColor {
        return bossBarColors.getOrNull(code) ?: BossBarColor.PURPLE
    }

    override fun translate(provider: BedrockProxyProvider, packet: BossEventPacket) {
        val uuid = UUID.nameUUIDFromBytes("FC_BOSSBAR_${packet.bossUniqueEntityId}".toByteArray())
        provider.packetOut(when(packet.action) {
            BossEventPacket.Action.CREATE -> {
                ClientboundBossEventPacket(uuid, Component.text(packet.title), packet.healthPercentage,
                    colorFromCode(packet.color), BossBarDivision.NONE,
                    packet.darkenSky == 1, false, false)
            }
            BossEventPacket.Action.REMOVE -> {
                ClientboundBossEventPacket(uuid)
            }
            BossEventPacket.Action.UPDATE_NAME -> {
                ClientboundBossEventPacket(uuid, Component.text(packet.title))
            }
            BossEventPacket.Action.UPDATE_PERCENTAGE -> {
                ClientboundBossEventPacket(uuid, packet.healthPercentage)
            }
            BossEventPacket.Action.UPDATE_STYLE -> {
                ClientboundBossEventPacket(uuid, colorFromCode(packet.color), BossBarDivision.NONE)
            }
            BossEventPacket.Action.UPDATE_PROPERTIES -> {
                ClientboundBossEventPacket(uuid, packet.darkenSky == 1, false, false)
            }
            else -> return // add a player to a boss fight is not supported by Java Edition
        })
    }
}